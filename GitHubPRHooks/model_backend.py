#!/usr/bin/env python3
"""
Backend LLM pentru rafinarea diagramelor de arhitectura.

Incearca sa apeleze (in ordine):
  1. Qwen2.5-7B fine-tuned  — ARCH_MODEL_ENDPOINT (env var, Colab/vLLM)
  2. diagram_generator FastAPI — http://localhost:8000
  3. Ollama local             — http://localhost:11434

Daca niciun backend nu e disponibil, returneaza None — caller-ul foloseste
rezultatul static ca fallback. Stdlib only, fara dependente externe.

Inlocuieste _refine_via_qwen_endpoint() cu endpoint-ul modelului antrenat.
"""

import json
import os
import re
import sys
import urllib.request
import urllib.error
from typing import Optional

from static_analyzer import DiagramResult

DEFAULT_BACKEND_URL = "http://localhost:8000"
OLLAMA_URL          = "http://localhost:11434"


# ---------------------------------------------------------------------------
# HTTP helpers (stdlib, fara httpx/requests)
# ---------------------------------------------------------------------------

def _http_post(url: str, payload: dict, timeout: int = 30) -> Optional[dict]:
    body = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url, data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception:
        return None


def _http_get_ok(url: str, timeout: int = 5) -> bool:
    try:
        with urllib.request.urlopen(url, timeout=timeout):
            return True
    except Exception:
        return False


# ---------------------------------------------------------------------------
# Availability checks
# ---------------------------------------------------------------------------

def is_qwen_available(endpoint: str = "") -> bool:
    """Verifica daca endpoint-ul Qwen e setat si accesibil."""
    ep = (endpoint or os.environ.get("ARCH_MODEL_ENDPOINT", "")).strip()
    return bool(ep)  # nu facem GET — endpoint-ul poate fi Colab/ngrok care nu are /health


def is_backend_available(base_url: str = DEFAULT_BACKEND_URL) -> bool:
    """Verifica daca diagram_generator FastAPI e pornit."""
    return _http_get_ok(f"{base_url}/health")


def is_ollama_available(base_url: str = OLLAMA_URL) -> bool:
    """Verifica daca Ollama e pornit local."""
    return _http_get_ok(f"{base_url}/api/tags")


# ---------------------------------------------------------------------------
# Prompt builders
# ---------------------------------------------------------------------------

def _build_qwen_prompt(diagram: DiagramResult) -> str:
    """
    Construieste prompt-ul in formatul pe care Qwen2.5-7B a fost antrenat.
    Potrivit cu schema din dataset:
      input.free_text_description + input.user_stories -> output.mermaid_diagram
    """
    stats = diagram.stats

    # Construieste descrierea libera din analiza statica
    parts = []
    controllers = [c["name"] for c in diagram.classes
                   if any(a in c["annotations"] for a in ["RestController", "Controller"])]
    services    = [c["name"] for c in diagram.classes if "Service"    in c["annotations"]]
    repos       = [c["name"] for c in diagram.classes if "Repository" in c["annotations"]]
    entities    = [c["name"] for c in diagram.classes if "Entity"     in c["annotations"]]

    if controllers:
        parts.append(f"REST controllers: {', '.join(controllers)}")
    if services:
        parts.append(f"services: {', '.join(services)}")
    if repos:
        parts.append(f"repositories: {', '.join(repos)}")
    if entities:
        parts.append(f"entities: {', '.join(entities)}")

    relations_text = "; ".join(
        f"{f} {rt} {t}" for f, rt, t in diagram.relations[:12]
    )

    description = (
        f"Java application with {stats['total']} classes. "
        + (", ".join(parts) + "." if parts else "")
        + (f" Relationships: {relations_text}." if relations_text else "")
    )

    # Formatul de prompt potrivit pentru modelul antrenat pe dataset-ul nostru
    return (
        f"Generate a Mermaid architecture diagram for the following system:\n\n"
        f"{description}\n\n"
        f"Current static diagram for reference:\n{diagram.mermaid}\n\n"
        f"Return only the improved Mermaid classDiagram code."
    )


def _build_generic_prompt(diagram: DiagramResult) -> str:
    """Prompt generic pentru Ollama / diagram_generator."""
    class_names = ", ".join(c["name"] for c in diagram.classes)
    relations_text = "\n".join(
        f"  - {f} {rt} {t}" for f, rt, t in diagram.relations
    ) or "  (none detected)"

    return (
        "You are a software architect. Analyze this Java codebase structure "
        "and improve the architecture diagram.\n\n"
        f"Detected classes: {class_names}\n\n"
        f"Detected relations:\n{relations_text}\n\n"
        f"Current Mermaid diagram (static AST):\n```\n{diagram.mermaid}\n```\n\n"
        "Return ONLY improved Mermaid classDiagram code, starting with 'classDiagram'."
    )


# ---------------------------------------------------------------------------
# Mermaid extractor
# ---------------------------------------------------------------------------

def _extract_mermaid(text: str) -> Optional[str]:
    """Extrage blocul classDiagram din raspunsul brut al LLM."""
    match = re.search(r"```(?:mermaid)?\s*(classDiagram[\s\S]+?)```", text)
    if match:
        return match.group(1).strip()
    text = text.strip()
    if text.startswith("classDiagram"):
        return text
    return None


def _with_refined_mermaid(original: DiagramResult, refined_mermaid: str) -> DiagramResult:
    """Returneaza un nou DiagramResult cu Mermaid rafinat, restul neschimbat."""
    return DiagramResult(
        mermaid=refined_mermaid,
        plantuml=original.plantuml,   # PlantUML static ramane ground truth
        classes=original.classes,
        relations=original.relations,
        source_files=original.source_files,
        changed_names=original.changed_names,
    )


# ---------------------------------------------------------------------------
# Backend-specific callers
# ---------------------------------------------------------------------------

def _refine_via_qwen_endpoint(
    diagram: DiagramResult,
    endpoint: str,
    timeout: int,
) -> Optional[DiagramResult]:
    """
    Apeleaza Qwen2.5-7B fine-tuned prin endpoint HTTP (Colab/vLLM/ngrok).

    Format request (identic cu hook-ul original):
        POST endpoint
        {"prompt": "...", "max_tokens": 1024, "format": "mermaid"}

    Format response:
        {"generated_text": "classDiagram ..."}

    Inlocuieste aceasta functie cu endpoint-ul modelului antrenat final.
    """
    payload = {
        "prompt":     _build_qwen_prompt(diagram),
        "max_tokens": 1024,
        "format":     "mermaid",
    }
    result = _http_post(endpoint, payload, timeout=timeout)
    if not result:
        return None

    raw = (
        result.get("generated_text") or
        result.get("text") or
        result.get("mermaid") or
        result.get("diagram") or
        ""
    )
    refined = _extract_mermaid(raw) if raw else None
    if not refined:
        print("  [model_backend] Qwen: raspuns fara classDiagram valid.", file=sys.stderr)
        return None
    return _with_refined_mermaid(diagram, refined)


def _refine_via_diagram_generator(
    diagram: DiagramResult,
    base_url: str,
    timeout: int,
) -> Optional[DiagramResult]:
    """Apeleaza diagram_generator-develop FastAPI pentru rafinare."""
    payload = {
        "description": _build_generic_prompt(diagram),
        "format": "mermaid",
    }
    result = _http_post(f"{base_url}/api/diagrams/generate", payload, timeout=timeout)
    if not result:
        return None

    raw = result.get("diagram") or result.get("mermaid") or result.get("content") or ""
    refined = _extract_mermaid(raw) if raw else None
    return _with_refined_mermaid(diagram, refined) if refined else None


def _ollama_stream(url: str, payload: dict, token_timeout: int = 30) -> Optional[str]:
    """
    Apeleaza Ollama in mod streaming.
    token_timeout = secunde maxime intre doua tokene consecutive.
    Evita timeout-ul global pe raspunsuri lungi (modele mari, prompturi complexe).
    """
    body = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url, data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        chunks = []
        with urllib.request.urlopen(req, timeout=token_timeout) as resp:
            for line in resp:
                line = line.strip()
                if not line:
                    continue
                chunk = json.loads(line.decode("utf-8"))
                chunks.append(chunk.get("response", ""))
                if chunk.get("done"):
                    break
        return "".join(chunks)
    except Exception as e:
        print(f"  [model_backend] Ollama stream eroare: {e}", file=sys.stderr)
        return None


def _refine_via_ollama(
    diagram: DiagramResult,
    base_url: str,
    model: str,
    timeout: int,
) -> Optional[DiagramResult]:
    """Apeleaza Ollama in mod streaming pentru a evita timeout pe raspunsuri lungi."""
    payload = {
        "model":  model,
        "prompt": _build_generic_prompt(diagram),
        "stream": True,
    }
    # Modele mari (14B+) pot lua >60s doar sa incarce contextul inainte de primul token.
    # Folosim max(timeout, 180) pentru Ollama local.
    raw = _ollama_stream(
        f"{base_url}/api/generate",
        payload,
        token_timeout=max(timeout, 180),
    )
    if not raw:
        return None

    refined = _extract_mermaid(raw)
    if not refined:
        print("  [model_backend] Ollama: raspuns fara classDiagram valid.", file=sys.stderr)
        print(f"  [model_backend] Raw (primele 200): {raw[:200]!r}", file=sys.stderr)
        return None
    return _with_refined_mermaid(diagram, refined)


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def refine_with_llm(
    diagram: DiagramResult,
    qwen_endpoint: str = "",
    backend_url: str = DEFAULT_BACKEND_URL,
    ollama_url: str = OLLAMA_URL,
    ollama_model: str = "qwen2.5:7b",
    timeout: int = 60,
) -> Optional[DiagramResult]:
    """
    Incearca sa rafineze diagrama prin LLM.

    Prioritate:
      1. Qwen2.5-7B (ARCH_MODEL_ENDPOINT sau qwen_endpoint param)
      2. diagram_generator FastAPI local
      3. Ollama local

    Returneaza None daca niciun backend nu e disponibil — caller-ul
    foloseste rezultatul static ca fallback.
    """
    # 1. Qwen2.5-7B fine-tuned
    ep = (qwen_endpoint or os.environ.get("ARCH_MODEL_ENDPOINT", "")).strip()
    if ep:
        print(f"  [model_backend] Incerc Qwen endpoint: {ep[:50]}...", file=sys.stderr)
        result = _refine_via_qwen_endpoint(diagram, ep, timeout)
        if result:
            print("  [model_backend] OK — rafinat via Qwen2.5-7B.", file=sys.stderr)
            return result
        print("  [model_backend] Qwen endpoint esuat.", file=sys.stderr)
    else:
        print("  [model_backend] ARCH_MODEL_ENDPOINT neset — sar Qwen.", file=sys.stderr)

    # 2. diagram_generator FastAPI
    print("  [model_backend] Incerc diagram_generator FastAPI...", file=sys.stderr)
    if is_backend_available(backend_url):
        result = _refine_via_diagram_generator(diagram, backend_url, timeout)
        if result:
            print("  [model_backend] OK — rafinat via diagram_generator.", file=sys.stderr)
            return result
        print("  [model_backend] Apel esuat.", file=sys.stderr)
    else:
        print("  [model_backend] diagram_generator indisponibil.", file=sys.stderr)

    # 3. Ollama
    print("  [model_backend] Incerc Ollama...", file=sys.stderr)
    if is_ollama_available(ollama_url):
        result = _refine_via_ollama(diagram, ollama_url, ollama_model, timeout)
        if result:
            print(f"  [model_backend] OK — rafinat via Ollama ({ollama_model}).", file=sys.stderr)
            return result
        print("  [model_backend] Raspuns Ollama invalid.", file=sys.stderr)
    else:
        print("  [model_backend] Ollama indisponibil.", file=sys.stderr)

    print("  [model_backend] Niciun backend disponibil — fallback la static.", file=sys.stderr)
    return None

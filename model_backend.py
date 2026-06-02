#!/usr/bin/env python3
"""
Backend LLM pentru rafinarea diagramelor de arhitectura.

Incearca sa apeleze (in ordine):
  1. diagram_generator API  — http://localhost:8000/diagrams/generate  (PRIMAR)
  2. Qwen2.5-7B fine-tuned  — ARCH_MODEL_ENDPOINT (env var, Colab/vLLM)
  3. Ollama local            — http://localhost:11434

Daca niciun backend nu e disponibil, returneaza None — caller-ul
foloseste rezultatul static ca fallback. Stdlib only, fara dependente externe.
"""

import json
import os
import re
import sys
import urllib.request
from typing import Optional

from static_analyzer import DiagramResult

DIAGRAM_GENERATOR_URL = "http://localhost:8000/diagrams/generate"
OLLAMA_URL            = "http://localhost:11434"


# ---------------------------------------------------------------------------
# HTTP helpers
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

def is_diagram_generator_available(url: str = DIAGRAM_GENERATOR_URL) -> bool:
    """Verifica daca diagram_generator e pornit."""
    base = url.split("/diagrams/")[0]
    return _http_get_ok(f"{base}/health")


def is_ollama_available(base_url: str = OLLAMA_URL) -> bool:
    return _http_get_ok(f"{base_url}/api/tags")


# ---------------------------------------------------------------------------
# Prompt builders
# ---------------------------------------------------------------------------

def _build_diagram_generator_prompt(diagram: DiagramResult) -> str:
    """
    Prompt pentru diagram_generator API.
    Trimite PlantUML-ul static si cere Mermaid inapoi — exact ca in compare_project.py.
    """
    return (
        f"Convert this PlantUML class diagram into a Mermaid architecture diagram:\n\n"
        f"{diagram.plantuml}"
    )


def _build_qwen_prompt(diagram: DiagramResult) -> str:
    """Prompt pentru Qwen2.5-7B fine-tuned (format compatibil cu dataset-ul de antrenare)."""
    stats = diagram.stats
    parts = []
    controllers = [c["name"] for c in diagram.classes
                   if any(a in c["annotations"] for a in ["RestController", "Controller"])]
    services    = [c["name"] for c in diagram.classes if "Service"    in c["annotations"]]
    repos       = [c["name"] for c in diagram.classes if "Repository" in c["annotations"]]
    entities    = [c["name"] for c in diagram.classes if "Entity"     in c["annotations"]]

    if controllers: parts.append(f"REST controllers: {', '.join(controllers)}")
    if services:    parts.append(f"services: {', '.join(services)}")
    if repos:       parts.append(f"repositories: {', '.join(repos)}")
    if entities:    parts.append(f"entities: {', '.join(entities)}")

    relations_text = "; ".join(f"{f} {rt} {t}" for f, rt, t in diagram.relations[:12])
    description = (
        f"Java application with {stats['total']} classes. "
        + (", ".join(parts) + "." if parts else "")
        + (f" Relationships: {relations_text}." if relations_text else "")
    )
    return (
        f"Generate a Mermaid architecture diagram for the following system:\n\n"
        f"{description}\n\n"
        f"Current static diagram for reference:\n{diagram.mermaid}\n\n"
        f"Return only the improved Mermaid classDiagram code."
    )


def _build_ollama_prompt(diagram: DiagramResult) -> str:
    """Prompt generic pentru Ollama."""
    class_names = ", ".join(c["name"] for c in diagram.classes)
    relations_text = "\n".join(
        f"  - {f} {rt} {t}" for f, rt, t in diagram.relations
    ) or "  (none detected)"
    return (
        "You are a software architect. Analyze this Java codebase and improve the architecture diagram.\n\n"
        f"Detected classes: {class_names}\n\n"
        f"Detected relations:\n{relations_text}\n\n"
        f"Current Mermaid diagram (static AST):\n```\n{diagram.mermaid}\n```\n\n"
        "Return ONLY improved Mermaid classDiagram code, starting with 'classDiagram'."
    )


# ---------------------------------------------------------------------------
# Mermaid extractor (pentru Ollama si Qwen care pot wrapa in code fences)
# ---------------------------------------------------------------------------

def _extract_mermaid(text: str) -> Optional[str]:
    match = re.search(r"```(?:mermaid)?\s*(classDiagram[\s\S]+?)```", text)
    if match:
        return match.group(1).strip()
    text = text.strip()
    if text.startswith("classDiagram"):
        return text
    return None


def _with_refined_mermaid(original: DiagramResult, refined_mermaid: str) -> DiagramResult:
    return DiagramResult(
        mermaid=refined_mermaid,
        plantuml=original.plantuml,
        classes=original.classes,
        relations=original.relations,
        source_files=original.source_files,
        changed_names=original.changed_names,
    )


# ---------------------------------------------------------------------------
# Backend callers
# ---------------------------------------------------------------------------

def _refine_via_diagram_generator(
    diagram: DiagramResult,
    url: str,
    model: str,
    timeout: int,
) -> Optional[DiagramResult]:
    """
    Apeleaza diagram_generator API.
    Payload identic cu compare_project.py.
    Raspunsul e Mermaid curat — returnat direct fara extractie.
    """
    payload = {
        "prompt":      _build_diagram_generator_prompt(diagram),
        "syntax_type": "mermaid",
        "subtype":     "auto",
        "model":       model,
        "options": {
            "agent": {"enabled": True, "max_iterations": 3}
        },
    }
    result = _http_post(url, payload, timeout=timeout)
    if not result:
        return None

    # Raspunsul API returneaza Mermaid curat in "code" sau "diagram.code"
    raw = result.get("code") or result.get("diagram", {}).get("code") or ""
    if not raw:
        print("  [model_backend] diagram_generator: raspuns gol.", file=sys.stderr)
        return None

    # Curata eventualele fences ramase
    mermaid = _extract_mermaid(raw) or raw.strip()
    return _with_refined_mermaid(diagram, mermaid)


def _refine_via_qwen_endpoint(
    diagram: DiagramResult,
    endpoint: str,
    timeout: int,
) -> Optional[DiagramResult]:
    """Apeleaza Qwen2.5-7B fine-tuned prin endpoint HTTP (Colab/vLLM/ngrok)."""
    payload = {
        "prompt":     _build_qwen_prompt(diagram),
        "max_tokens": 1024,
        "format":     "mermaid",
    }
    result = _http_post(endpoint, payload, timeout=timeout)
    if not result:
        return None

    raw = (
        result.get("generated_text") or result.get("text") or
        result.get("mermaid") or result.get("diagram") or ""
    )
    refined = _extract_mermaid(raw) if raw else None
    if not refined:
        print("  [model_backend] Qwen: raspuns fara classDiagram valid.", file=sys.stderr)
        return None
    return _with_refined_mermaid(diagram, refined)


def _ollama_stream(url: str, payload: dict, token_timeout: int = 180) -> Optional[str]:
    """Apeleaza Ollama in mod streaming (evita timeout pe modele mari)."""
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
    payload = {
        "model":  model,
        "prompt": _build_ollama_prompt(diagram),
        "stream": True,
    }
    raw = _ollama_stream(f"{base_url}/api/generate", payload, token_timeout=max(timeout, 180))
    if not raw:
        return None
    refined = _extract_mermaid(raw)
    if not refined:
        print(f"  [model_backend] Ollama: raspuns invalid. Raw: {raw[:200]!r}", file=sys.stderr)
        return None
    return _with_refined_mermaid(diagram, refined)


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def refine_with_llm(
    diagram: DiagramResult,
    diagram_generator_url: str = DIAGRAM_GENERATOR_URL,
    diagram_generator_model: str = "qwen2.5:7b",
    qwen_endpoint: str = "",
    ollama_url: str = OLLAMA_URL,
    ollama_model: str = "qwen2.5:7b",
    timeout: int = 120,
) -> Optional[DiagramResult]:
    """
    Rafinare diagrama prin LLM.

    Prioritate:
      1. diagram_generator API  (localhost:8000/diagrams/generate)
      2. Qwen2.5-7B fine-tuned  (ARCH_MODEL_ENDPOINT)
      3. Ollama local

    Returneaza None daca niciun backend nu e disponibil.
    """
    # 1. diagram_generator API — principalul backend
    print(f"  [model_backend] Incerc diagram_generator API ({diagram_generator_url})...", file=sys.stderr)
    if is_diagram_generator_available(diagram_generator_url):
        result = _refine_via_diagram_generator(diagram, diagram_generator_url, diagram_generator_model, timeout)
        if result:
            print("  [model_backend] OK — rafinat via diagram_generator.", file=sys.stderr)
            return result
        print("  [model_backend] Apel esuat.", file=sys.stderr)
    else:
        print("  [model_backend] diagram_generator indisponibil.", file=sys.stderr)

    # 2. Qwen fine-tuned endpoint
    ep = (qwen_endpoint or os.environ.get("ARCH_MODEL_ENDPOINT", "")).strip()
    if ep:
        print(f"  [model_backend] Incerc Qwen endpoint: {ep[:60]}...", file=sys.stderr)
        result = _refine_via_qwen_endpoint(diagram, ep, timeout)
        if result:
            print("  [model_backend] OK — rafinat via Qwen2.5-7B.", file=sys.stderr)
            return result
        print("  [model_backend] Qwen esuat.", file=sys.stderr)

    # 3. Ollama local
    print("  [model_backend] Incerc Ollama...", file=sys.stderr)
    if is_ollama_available(ollama_url):
        result = _refine_via_ollama(diagram, ollama_url, ollama_model, timeout)
        if result:
            print(f"  [model_backend] OK — rafinat via Ollama ({ollama_model}).", file=sys.stderr)
            return result
        print("  [model_backend] Ollama invalid.", file=sys.stderr)
    else:
        print("  [model_backend] Ollama indisponibil.", file=sys.stderr)

    print("  [model_backend] Niciun backend disponibil — fallback la static.", file=sys.stderr)
    return None

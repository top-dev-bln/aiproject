#!/usr/bin/env python3
"""
pre_push_hook.py — logica hook-ului git pre-push.

Genereaza diagrame de arhitectura pentru fisierele Java modificate fata
de main/master si le salveaza in docs/architecture/.

Nu blocheaza niciodata push-ul (exit 0 intotdeauna).

Instalare:
    python install_hooks.py

Variabile de mediu:
    ARCH_MODEL_ENDPOINT   URL catre Qwen2.5-7B fine-tuned (Colab / vLLM / ngrok)
                          Daca neset, se incearca Ollama local ca fallback.

Flux:
    1. Analiza statica Java  -> DiagramResult  (via static_analyzer.py)
    2. Rafinare LLM optional -> DiagramResult  (via model_backend.py)
    3. Salvare docs/architecture/{branch}_{timestamp}.mmd / .puml / latest.json
"""

import json
import os
import subprocess
import sys
from datetime import datetime
from pathlib import Path

# Git hooks ruleaza din radacina repo-ului — adaugam cwd in path
_REPO_ROOT = Path.cwd()
sys.path.insert(0, str(_REPO_ROOT))

from static_analyzer import analyze, load_changed_java_files, EMPTY_RESULT
from model_backend import refine_with_llm

DOCS_DIR   = _REPO_ROOT / "docs" / "architecture"
ENDPOINT   = os.environ.get("ARCH_MODEL_ENDPOINT", "").strip()
# Limita de fisiere analizate — evita blocaje pe repo-uri cu mii de fisiere modificate
MAX_FILES  = int(os.environ.get("ARCH_MAX_FILES", "50"))


# ---------------------------------------------------------------------------
# Git helpers
# ---------------------------------------------------------------------------

def _run_git(*args, timeout: int = 15) -> str:
    """Ruleaza o comanda git si returneaza stdout, sau '' daca esueaza."""
    try:
        r = subprocess.run(
            ["git", *args],
            capture_output=True, text=True,
            cwd=str(_REPO_ROOT), timeout=timeout
        )
        return r.stdout.strip() if r.returncode == 0 else ""
    except Exception:
        return ""


def get_current_branch() -> str:
    return _run_git("rev-parse", "--abbrev-ref", "HEAD") or "unknown"


def get_changed_java_files() -> list:
    """
    Returneaza fisierele .java modificate fata de main/master,
    ca tupluri (display_path, abs_path) — compatibil cu static_analyzer.
    Limitat la MAX_FILES pentru a evita blocaje pe repo-uri mari.
    """
    for base in ["origin/main", "origin/master", "main", "master"]:
        output = _run_git("diff", "--name-only", base, "HEAD")
        if output:
            files = []
            for f in output.splitlines():
                if f.endswith(".java") and os.path.exists(f):
                    files.append((f, str((_REPO_ROOT / f).resolve())))
            if len(files) > MAX_FILES:
                print(
                    f"[arch-hook] {len(files)} fisiere Java modificate — "
                    f"analizam primele {MAX_FILES} (seteaza ARCH_MAX_FILES pentru mai multe)."
                )
                files = files[:MAX_FILES]
            return files
    return []


# ---------------------------------------------------------------------------
# changed_files.txt temporar pentru static_analyzer.analyze()
# ---------------------------------------------------------------------------

def _write_temp_changed(changed_files: list) -> Path:
    """Scrie fisierele modificate intr-un fisier temporar pentru analyze()."""
    tmp = _REPO_ROOT / ".arch_changed_tmp.txt"
    tmp.write_text(
        "\n".join(disp for disp, _ in changed_files),
        encoding="utf-8"
    )
    return tmp


def _cleanup_temp(tmp: Path):
    try:
        tmp.unlink()
    except Exception:
        pass


# ---------------------------------------------------------------------------
# Salvare diagrame
# ---------------------------------------------------------------------------

def save_diagrams(static_mermaid: str, llm_mermaid: str, plantuml: str, branch: str):
    """
    Salveaza diagramele in docs/architecture/.
    Structura:
        {branch}_{timestamp}.mmd         — Mermaid static (ground truth)
        {branch}_{timestamp}_llm.mmd     — Mermaid LLM (daca disponibil)
        {branch}_{timestamp}.puml        — PlantUML static
        latest.json                      — metadata pentru GitHub Action
    """
    DOCS_DIR.mkdir(parents=True, exist_ok=True)

    safe_branch = branch.replace("/", "_").replace(" ", "_")
    timestamp   = datetime.now().strftime("%Y%m%d_%H%M%S")
    prefix      = DOCS_DIR / f"{safe_branch}_{timestamp}"

    saved = {}

    if static_mermaid:
        p = Path(f"{prefix}.mmd")
        p.write_text(static_mermaid, encoding="utf-8")
        saved["mermaid_static"] = str(p)
        print(f"[arch-hook] Mermaid static salvat: {p}")

    if llm_mermaid:
        p = Path(f"{prefix}_llm.mmd")
        p.write_text(llm_mermaid, encoding="utf-8")
        saved["mermaid_llm"] = str(p)
        print(f"[arch-hook] Mermaid LLM salvat:   {p}")

    if plantuml:
        p = Path(f"{prefix}.puml")
        p.write_text(plantuml, encoding="utf-8")
        saved["plantuml"] = str(p)
        print(f"[arch-hook] PlantUML salvat:       {p}")

    meta = {
        "branch":    branch,
        "timestamp": timestamp,
        "endpoint":  ENDPOINT or None,
        **saved,
    }
    meta_path = DOCS_DIR / "latest.json"
    meta_path.write_text(json.dumps(meta, indent=2), encoding="utf-8")
    print(f"[arch-hook] Metadata: {meta_path}")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    changed_files = get_changed_java_files()

    if not changed_files:
        print("[arch-hook] Nicio modificare Java fata de main — sar peste generare.")
        sys.exit(0)

    print(f"[arch-hook] {len(changed_files)} fisiere Java modificate — analiza statica...")

    # Scrie fisierul temporar pentru analyze()
    tmp = _write_temp_changed(changed_files)

    try:
        static_result = analyze(str(tmp), include_context=True)
    finally:
        _cleanup_temp(tmp)

    if static_result.is_empty():
        print("[arch-hook] Nu s-au putut parsa clasele Java — continuă push.")
        sys.exit(0)

    print(
        f"[arch-hook] Clase: {static_result.stats['total']},"
        f" Relatii: {static_result.stats['relations']}"
    )

    # Rafinare LLM (optional — nu blocheaza niciodata)
    llm_result = None
    if ENDPOINT:
        print(f"[arch-hook] Incerc Qwen2.5-7B @ {ENDPOINT[:50]}...")
    else:
        print("[arch-hook] ARCH_MODEL_ENDPOINT neset — incerc Ollama local...")

    try:
        llm_result = refine_with_llm(static_result, qwen_endpoint=ENDPOINT, timeout=30)
    except Exception as e:
        print(f"[arch-hook] Rafinare LLM esuat: {e} — continuă cu static.")

    branch = get_current_branch()
    save_diagrams(
        static_mermaid=static_result.mermaid,
        llm_mermaid=llm_result.mermaid if llm_result else "",
        plantuml=static_result.plantuml,
        branch=branch,
    )

    # Nu blocam niciodata push-ul
    sys.exit(0)


if __name__ == "__main__":
    main()

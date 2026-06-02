#!/usr/bin/env python3
"""
Genereaza comentariul PR cu diagrame de arhitectura.

Responsabilitate unica: formateaza comentariul Markdown pentru GitHub PR.
  - Analiza statica  → static_analyzer.py
  - Rafinare LLM     → model_backend.py  (via localhost:8000/diagrams/generate)
  - Formatare PR     → acest fisier

Moduri (--mode):
  static   Doar analiza statica AST (default, folosit in CI fara backend)
  llm      Analiza statica + rafinare via diagram_generator API, fallback la static
  compare  Ambele side-by-side (Static vs LLM) pentru evaluare
"""

import os
import sys
import argparse
from pathlib import Path
from typing import Optional

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from static_analyzer import DiagramResult, analyze, load_changed_java_files
from model_backend import refine_with_llm, DIAGRAM_GENERATOR_URL


MARKER = "<!-- architecture-diagram-bot -->"


# ---------------------------------------------------------------------------
# Formatting helpers
# ---------------------------------------------------------------------------

def _stats_line(r: DiagramResult) -> str:
    s = r.stats
    parts = []
    if s["controllers"]: parts.append(f"**{s['controllers']}** Controller{'s' if s['controllers']>1 else ''}")
    if s["services"]:    parts.append(f"**{s['services']}** Service{'s' if s['services']>1 else ''}")
    if s["repositories"]:
        parts.append(f"**{s['repositories']}** {'Repositories' if s['repositories']>1 else 'Repository'}")
    if s["entities"]:
        parts.append(f"**{s['entities']}** {'Entities' if s['entities']>1 else 'Entity'}")
    base = " · ".join(parts) if parts else ""
    tail = f"**{s['total']}** clase · **{s['relations']}** relatii"
    return f"{base} · {tail}" if base else tail


def _files_section(changed_files: list) -> str:
    return "\n".join(f"- `{disp}`" for disp, _ in sorted(changed_files))


def _highlight_line(r: DiagramResult) -> str:
    if not r.changed_names:
        return ""
    names = ", ".join(f"`{n}`" for n in sorted(r.changed_names))
    return f"\n**Clase modificate:** {names}\n"


def _mermaid_block(mermaid: str) -> str:
    return f"```mermaid\n{mermaid}\n```"


def _puml_details(plantuml: str) -> str:
    return (
        "<details>\n"
        "<summary>PlantUML (click pentru expandare)</summary>\n\n"
        f"```plantuml\n{plantuml}\n```\n\n"
        "</details>"
    )


# ---------------------------------------------------------------------------
# Mode formatters
# ---------------------------------------------------------------------------

def format_static(result: DiagramResult, changed_files: list) -> str:
    return f"""{MARKER}
## Architecture Diagram Update

**Fisiere Java modificate ({len(changed_files)}):**
{_files_section(changed_files)}
{_highlight_line(result)}
**Componente detectate:** {_stats_line(result)}

---

### Diagrama (analiza statica)

{_mermaid_block(result.mermaid)}

{_puml_details(result.plantuml)}

---
> Generata automat · **mod: static** · [static\\_analyzer.py](./static_analyzer.py)
"""


def format_llm(
    static_result: DiagramResult,
    llm_result: Optional[DiagramResult],
    changed_files: list,
) -> str:
    used = llm_result if llm_result else static_result
    source_note = "rafinata via diagram_generator API" if llm_result else "fallback static (API indisponibil)"
    return f"""{MARKER}
## Architecture Diagram Update

**Fisiere Java modificate ({len(changed_files)}):**
{_files_section(changed_files)}
{_highlight_line(static_result)}
**Componente detectate:** {_stats_line(static_result)}

---

### Diagrama ({source_note})

{_mermaid_block(used.mermaid)}

{_puml_details(used.plantuml)}

---
> Generata automat · **mod: llm** · [static\\_analyzer.py](./static_analyzer.py) + [model\\_backend.py](./model_backend.py)
"""


def format_compare(
    static_result: DiagramResult,
    llm_result: Optional[DiagramResult],
    changed_files: list,
) -> str:
    if llm_result:
        llm_section = f"### Diagrama LLM (diagram_generator API)\n\n{_mermaid_block(llm_result.mermaid)}"
    else:
        llm_section = (
            "### Diagrama LLM\n\n"
            "> diagram_generator API indisponibil — porneste backend-ul si incearca din nou.\n"
        )

    return f"""{MARKER}
## Architecture Diagram Update — Static vs LLM

**Fisiere Java modificate ({len(changed_files)}):**
{_files_section(changed_files)}
{_highlight_line(static_result)}
**Componente detectate:** {_stats_line(static_result)}

---

### Diagrama Static (analiza AST)

{_mermaid_block(static_result.mermaid)}

{llm_section}

{_puml_details(static_result.plantuml)}

---
> Generata automat · **mod: compare** · [static\\_analyzer.py](./static_analyzer.py) + [model\\_backend.py](./model_backend.py)
"""


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description="Genereaza diagrame de arhitectura pentru comentariu PR"
    )
    parser.add_argument("--changed-files", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument(
        "--mode", choices=["static", "llm", "compare"], default="static",
        help="static | llm | compare (default: static)"
    )
    parser.add_argument(
        "--no-llm", action="store_true",
        help="Forteaza modul static (CI fara backend)"
    )
    parser.add_argument("--no-context", action="store_true")
    parser.add_argument(
        "--backend-url", default=DIAGRAM_GENERATOR_URL,
        help=f"URL diagram_generator API (default: {DIAGRAM_GENERATOR_URL})"
    )
    parser.add_argument(
        "--backend-model", default="qwen2.5:7b",
        help="Model folosit de diagram_generator (default: qwen2.5:7b)"
    )
    args = parser.parse_args()

    mode = "static" if args.no_llm else args.mode
    print(f"Mod: {mode}", file=sys.stderr)

    print("Analiza statica...", file=sys.stderr)
    static_result = analyze(args.changed_files, include_context=not args.no_context)
    changed_files = load_changed_java_files(args.changed_files)

    if not changed_files or static_result.is_empty():
        print("Niciun fisier .java modificat sau parsabil.", file=sys.stderr)
        Path(args.output).write_text("<!-- no-java-changes -->", encoding="utf-8")
        sys.exit(0)

    print(
        f"  Clase: {static_result.stats['total']},"
        f" Relatii: {static_result.stats['relations']}",
        file=sys.stderr,
    )

    if mode == "static":
        comment = format_static(static_result, changed_files)

    elif mode == "llm":
        print(f"Rafinare LLM via {args.backend_url}...", file=sys.stderr)
        llm_result = refine_with_llm(
            static_result,
            diagram_generator_url=args.backend_url,
            diagram_generator_model=args.backend_model,
        )
        comment = format_llm(static_result, llm_result, changed_files)

    else:  # compare
        print(f"Rafinare LLM pentru comparatie via {args.backend_url}...", file=sys.stderr)
        llm_result = refine_with_llm(
            static_result,
            diagram_generator_url=args.backend_url,
            diagram_generator_model=args.backend_model,
        )
        comment = format_compare(static_result, llm_result, changed_files)

    Path(args.output).write_text(comment, encoding="utf-8")
    print(f"Comment scris in: {args.output}", file=sys.stderr)


if __name__ == "__main__":
    main()

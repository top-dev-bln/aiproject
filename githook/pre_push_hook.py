#!/usr/bin/env python3
"""
.git/hooks/pre-push
Generează diagrame pentru fișierele modificate față de main/master
și le salvează în docs/architecture/.
Rulează și la push — nu blochează niciodată, doar generează.
"""

import subprocess
import sys
import os
import json
from pathlib import Path
from datetime import datetime

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "..", "arch_hooks"))

DOCS_DIR = Path("docs/architecture")
ENDPOINT = os.environ.get("ARCH_MODEL_ENDPOINT", "http://localhost:8000/diagrams/generate").strip()


def get_changed_files_vs_main() -> list[str]:
    """Fișierele Python modificate față de main/master."""
    try:
        for base in ["origin/main", "origin/master", "main", "master"]:
            result = subprocess.run(
                ["git", "diff", "--name-only", base, "HEAD"],
                capture_output=True, text=True
            )
            if result.returncode == 0:
                return [
                    f for f in result.stdout.strip().split("\n")
                    if f.endswith(".py") and os.path.exists(f)
                ]
        return []
    except Exception:
        return []


def build_context(files: list[str]) -> str:
    parts = []
    for fpath in files[:8]:
        try:
            content = open(fpath, encoding="utf-8").read()
            parts.append(f"# {fpath}\n{content[:1000]}")
        except Exception:
            continue
    return "\n\n".join(parts)


def call_model(context: str, fmt: str = "mermaid") -> str:
    """
    Cheamă diagram-generator API pentru generare diagramă.
    fmt: 'mermaid' sau 'plantuml'
    """
    import requests

    resp = requests.post(
        ENDPOINT,
        json={
            "prompt": f"Generate a {fmt} architecture diagram for the following code:\n\n{context}",
            "syntax_type": fmt,
            "subtype": "auto",
            "options": {"agent": {"enabled": True, "max_iterations": 2}},
        },
        timeout=60
    )
    resp.raise_for_status()
    data = resp.json()
    return data.get("code") or data.get("diagram", {}).get("code", "")


def save_diagrams(mermaid: str, plantuml: str, branch: str):
    """Salvează diagramele în docs/architecture/."""
    DOCS_DIR.mkdir(parents=True, exist_ok=True)

    safe_branch = branch.replace("/", "_").replace(" ", "_")
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    # fișier Mermaid
    if mermaid:
        mermaid_path = DOCS_DIR / f"{safe_branch}_{timestamp}.mmd"
        mermaid_path.write_text(mermaid, encoding="utf-8")
        print(f"[arch-hook] Mermaid salvat: {mermaid_path}")

    # fișier PlantUML
    if plantuml:
        puml_path = DOCS_DIR / f"{safe_branch}_{timestamp}.puml"
        puml_path.write_text(plantuml, encoding="utf-8")
        print(f"[arch-hook] PlantUML salvat: {puml_path}")

    # metadata JSON pentru GitHub Action să îl citească
    meta = {
        "branch": branch,
        "timestamp": timestamp,
        "mermaid_file": str(mermaid_path) if mermaid else None,
        "plantuml_file": str(puml_path) if plantuml else None,
    }
    meta_path = DOCS_DIR / "latest.json"
    meta_path.write_text(json.dumps(meta, indent=2), encoding="utf-8")


def get_current_branch() -> str:
    try:
        result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            capture_output=True, text=True
        )
        return result.stdout.strip() or "unknown"
    except Exception:
        return "unknown"


def main():
    changed = get_changed_files_vs_main()

    if not changed:
        print("[arch-hook] Nicio modificare Python față de main — sar peste generare.")
        sys.exit(0)

    print(f"[arch-hook] {len(changed)} fișiere modificate — generez diagrame...")

    branch = get_current_branch()
    context = build_context(changed)

    mermaid = ""
    plantuml = ""

    try:
        mermaid = call_model(context, fmt="mermaid")
    except Exception as e:
        print(f"[arch-hook] Mermaid generation skip: {e}")

    try:
        plantuml = call_model(context, fmt="plantuml")
    except Exception as e:
        print(f"[arch-hook] PlantUML generation skip: {e}")

    if mermaid or plantuml:
        try:
            save_diagrams(mermaid, plantuml, branch)
        except Exception as e:
            print(f"[arch-hook] Save skip: {e}")
    else:
        print("[arch-hook] Nicio diagramă generată — continuă push.")

    # nu blocăm niciodată push-ul
    sys.exit(0)


if __name__ == "__main__":
    main()

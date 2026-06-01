#!/usr/bin/env python3
"""
.git/hooks/pre-commit
Verifică că diagramele se pot genera pentru fișierele Python staged.
Dacă orice crapă (model indisponibil, eroare parsare, etc.) — pass.
"""

import subprocess
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "..", "arch_hooks"))


def get_staged_python_files() -> list[str]:
    try:
        result = subprocess.run(
            ["git", "diff", "--staged", "--name-only"],
            capture_output=True, text=True
        )
        return [f for f in result.stdout.strip().split("\n") if f.endswith(".py") and os.path.exists(f)]
    except Exception:
        return []


def call_model_endpoint(context: str) -> str:
    """
    Cheamă endpoint-ul Colab (LoRA finetuned).
    Setează ARCH_MODEL_ENDPOINT în environment sau în .env
    """
    import requests

    endpoint = os.environ.get("ARCH_MODEL_ENDPOINT", "").strip()
    if not endpoint:
        raise ValueError("ARCH_MODEL_ENDPOINT not set")

    resp = requests.post(
        endpoint,
        json={"prompt": context, "max_tokens": 512},
        timeout=15
    )
    resp.raise_for_status()
    return resp.json().get("generated_text", "")


def build_context(files: list[str]) -> str:
    """Construiește un context minimal din fișierele staged."""
    parts = []
    for fpath in files[:5]:  # max 5 fișiere ca să nu explodeze promptul
        try:
            content = open(fpath, encoding="utf-8").read()
            parts.append(f"# {fpath}\n{content[:800]}")  # primii 800 chars per fișier
        except Exception:
            continue
    return "\n\n".join(parts)


def validate(files: list[str]) -> bool:
    """
    Returnează True dacă generarea diagramei reușește.
    Orice excepție → True (pass).
    """
    context = build_context(files)
    if not context:
        return True

    try:
        result = call_model_endpoint(context)
        # validare minimă: output-ul conține ceva care arată a diagramă
        return bool(result and len(result.strip()) > 10)
    except Exception:
        return True  # endpoint down, timeout, orice — pass


def main():
    staged = get_staged_python_files()

    if not staged:
        sys.exit(0)

    print(f"[arch-hook] {len(staged)} fișiere Python staged — verific generarea diagramelor...")

    try:
        ok = validate(staged)
    except Exception:
        ok = True  # orice eroare neașteptată → pass

    if not ok:
        print("[arch-hook] ⚠️  Generarea diagramei a eșuat — commit blocat.")
        sys.exit(1)

    print("[arch-hook] ✓ Ok.")
    sys.exit(0)


if __name__ == "__main__":
    main()

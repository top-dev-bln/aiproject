#!/usr/bin/env python3
"""
Instaleaza hook-urile git pentru proiect.

Ruleaza o singura data dupa clone sau cand hook-urile se schimba:
    python install_hooks.py

Ce face:
    Creeaza .git/hooks/pre-push — un wrapper shell care apeleaza
    pre_push_hook.py din radacina repo-ului.

    Daca exista deja un hook, il suprascriem (avertizare afisata).
"""

import os
import stat
import sys
from pathlib import Path

REPO_ROOT  = Path(__file__).resolve().parent
HOOKS_DIR  = REPO_ROOT / ".git" / "hooks"
HOOK_SCRIPT = REPO_ROOT / "pre_push_hook.py"


# Continutul hook-ului generat in .git/hooks/pre-push
# Functioneaza pe Unix (bash) si pe Windows (Git Bash / MSYS2)
HOOK_CONTENT = """\
#!/usr/bin/env bash
# Auto-generat de install_hooks.py — nu edita manual.
# Logica se afla in pre_push_hook.py din radacina repo-ului.

REPO_ROOT="$(git rev-parse --show-toplevel)"
python "$REPO_ROOT/pre_push_hook.py"
exit $?
"""


def check_prereqs():
    if not HOOKS_DIR.exists():
        print(f"[install] Eroare: {HOOKS_DIR} nu exista.")
        print("         Asigura-te ca esti in radacina unui repo git.")
        sys.exit(1)

    if not HOOK_SCRIPT.exists():
        print(f"[install] Eroare: {HOOK_SCRIPT} nu exista.")
        sys.exit(1)


def install_hook(hook_name: str, content: str):
    hook_path = HOOKS_DIR / hook_name

    if hook_path.exists():
        existing = hook_path.read_text(encoding="utf-8")
        if "Auto-generat de install_hooks.py" in existing:
            print(f"[install] Actualizez {hook_name}...")
        else:
            print(f"[install] ATENTIE: {hook_path} exista si nu e generat de noi.")
            print("         Il suprascriem. Backup la .git/hooks/{hook_name}.bak")
            hook_path.rename(HOOKS_DIR / f"{hook_name}.bak")

    hook_path.write_text(content, encoding="utf-8")

    # Seteaza permisiuni executabile pe Unix (ignorat pe Windows, dar util pe CI)
    current = hook_path.stat().st_mode
    hook_path.chmod(current | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

    print(f"[install] Hook instalat: {hook_path}")


def verify_python_deps():
    """Verifica ca modulele necesare sunt importabile."""
    missing = []
    for mod in ["static_analyzer", "model_backend"]:
        try:
            __import__(mod)
        except ImportError:
            missing.append(mod)

    if missing:
        print(f"[install] ATENTIE: module lipsa: {', '.join(missing)}")
        print("         Asigura-te ca rulezi din radacina proiectului.")
    else:
        print("[install] Dependente Python OK.")


def main():
    print("=== install_hooks.py ===\n")
    check_prereqs()

    install_hook("pre-push", HOOK_CONTENT)

    print()
    verify_python_deps()

    print()
    print("=== Gata! ===")
    print()
    print("Hook pre-push instalat. La urmatorul 'git push':")
    print("  - Se analizeaza fisierele Java modificate fata de main")
    print("  - Se genereaza diagrame in docs/architecture/")
    print()
    print("Pentru a folosi Qwen2.5-7B fine-tuned, seteaza:")
    print("  export ARCH_MODEL_ENDPOINT=https://your-colab-ngrok-url/generate")
    print()
    print("Fara endpoint: hook-ul incearca Ollama local, apoi continua fara LLM.")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Sterge toate fisierele si folderele Git dintr-un director cu proiecte descarcate.
Ruleaza: python clean_git.py <cale_catre_dataset>
"""

import os
import shutil
import sys
import argparse

GIT_DIRS = {".git"}
GIT_FILES = {
    ".gitignore",
    ".gitmodules",
    ".gitattributes",
    ".gitkeep",
    ".github",  # folder
}


def clean_git_artifacts(root_dir: str, dry_run: bool = False) -> None:
    removed_dirs = 0
    removed_files = 0

    for dirpath, dirnames, filenames in os.walk(root_dir, topdown=True):
        # Sterge foldere .git si .github
        for d in list(dirnames):
            if d in GIT_DIRS or d in GIT_FILES:
                full_path = os.path.join(dirpath, d)
                print(f"[DIR]  {full_path}")
                if not dry_run:
                    shutil.rmtree(full_path)
                dirnames.remove(d)  # nu mai intra recursiv
                removed_dirs += 1

        # Sterge fisiere git
        for f in filenames:
            if f in GIT_FILES:
                full_path = os.path.join(dirpath, f)
                print(f"[FILE] {full_path}")
                if not dry_run:
                    os.remove(full_path)
                removed_files += 1

    print(f"\nGata! {removed_dirs} foldere + {removed_files} fisiere {'(dry run)' if dry_run else 'sterse'}.")


def main():
    parser = argparse.ArgumentParser(description="Sterge artefactele Git din proiecte descarcate.")
    parser.add_argument("directory", help="Directorul radacina cu proiectele")
    parser.add_argument("--dry-run", action="store_true", help="Doar afiseaza ce ar fi sters, fara sa stearga")
    args = parser.parse_args()

    if not os.path.isdir(args.directory):
        print(f"Eroare: '{args.directory}' nu este un director valid.")
        sys.exit(1)

    if args.dry_run:
        print("=== DRY RUN — nimic nu se sterge ===\n")

    clean_git_artifacts(args.directory, dry_run=args.dry_run)


if __name__ == "__main__":
    main()

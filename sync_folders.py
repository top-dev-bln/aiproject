#!/usr/bin/env python3
import os
import argparse

PUML_DIR = "diagrame_puml"
JSON_DIR = "json_output"

def sync(dry_run=False):
    puml_names = {os.path.splitext(f)[0] for f in os.listdir(PUML_DIR) if f.endswith(".puml")}
    json_names = {os.path.splitext(f)[0] for f in os.listdir(JSON_DIR)  if f.endswith(".json")}

    only_puml = puml_names - json_names
    only_json = json_names - puml_names

    label = "[DRY-RUN]" if dry_run else "[STERS]"

    for name in sorted(only_puml):
        path = os.path.join(PUML_DIR, f"{name}.puml")
        print(f"{label} {path}  (fara JSON)")
        if not dry_run:
            os.remove(path)

    for name in sorted(only_json):
        path = os.path.join(JSON_DIR, f"{name}.json")
        print(f"{label} {path}  (fara PUML)")
        if not dry_run:
            os.remove(path)

    print(f"\nFara pereche: {len(only_puml)} PUML, {len(only_json)} JSON")
    if not dry_run:
        print(f"Ramas: {len(puml_names & json_names)} perechi")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true", help="Doar afiseaza, nu sterge")
    args = parser.parse_args()
    sync(dry_run=args.dry_run)

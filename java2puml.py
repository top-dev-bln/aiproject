#!/usr/bin/env python3
"""
Inlocuitor pentru PlantUMLGenerator.jar
Genereaza diagrama PlantUML din fisiere .java

Utilizare: python java2puml.py <director_sursa> <output.puml>
"""

import os
import re
import sys


def parse_java_file(path):
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()

    # Package
    pkg_match = re.search(r"^\s*package\s+([\w.]+)\s*;", content, re.MULTILINE)
    package = pkg_match.group(1) if pkg_match else ""

    # Imports (doar din acelasi proiect - filtrate mai jos)
    imports = re.findall(r"^\s*import\s+([\w.]+)\s*;", content, re.MULTILINE)

    # Tip principal: class / interface / enum / record
    type_match = re.search(
        r"(?:public\s+)?(?:abstract\s+)?(class|interface|enum|record)\s+(\w+)"
        r"(?:\s+extends\s+([\w<>, ]+?))?(?:\s+implements\s+([\w<>, ]+?))?(?:\s*\{|\s*<)",
        content,
    )
    if not type_match:
        return None

    kind = type_match.group(1)       # class / interface / enum / record
    name = type_match.group(2)
    extends = type_match.group(3)
    implements_raw = type_match.group(4)

    # Curata generics
    def clean(s):
        return re.sub(r"<[^>]+>", "", s).strip() if s else None

    extends = clean(extends)
    implements = [clean(i.strip()) for i in implements_raw.split(",")] if implements_raw else []

    # Annotations pe clasa
    annotations = re.findall(r"@(\w+)(?:\([^)]*\))?", content[:type_match.start()])

    # Campuri
    fields = re.findall(
        r"(?:private|protected|public|final|static)\s+(?:static\s+|final\s+)*([\w<>\[\]]+)\s+(\w+)\s*[;=,)]",
        content,
    )

    # Metode publice
    methods = re.findall(
        r"(?:public|protected)\s+(?:static\s+|final\s+|abstract\s+)*([\w<>\[\]]+)\s+(\w+)\s*\([^)]*\)",
        content,
    )

    return {
        "package": package,
        "name": name,
        "kind": kind,
        "extends": extends,
        "implements": implements,
        "annotations": annotations,
        "fields": fields,
        "methods": methods,
        "imports": imports,
        "full_name": f"{package}.{name}" if package else name,
    }


def generate_puml(src_dir, output_path):
    classes = []

    for root, _, files in os.walk(src_dir):
        for fname in files:
            if not fname.endswith(".java"):
                continue
            fpath = os.path.join(root, fname)
            result = parse_java_file(fpath)
            if result:
                classes.append(result)

    if not classes:
        print("Niciun fisier .java gasit.")
        sys.exit(1)

    # Colecteaza toate numele cunoscute
    known_names = {c["name"] for c in classes}
    known_full = {c["full_name"] for c in classes}

    lines = ["@startuml", ""]

    # Grupeaza pe pachete
    packages = {}
    for c in classes:
        packages.setdefault(c["package"], []).append(c)

    for pkg, members in sorted(packages.items()):
        if pkg:
            lines.append(f'package {pkg} {{')
        for c in members:
            stereo = ""
            if "RestController" in c["annotations"] or "Controller" in c["annotations"]:
                stereo = " <<Controller>>"
            elif "Service" in c["annotations"]:
                stereo = " <<Service>>"
            elif "Repository" in c["annotations"]:
                stereo = " <<Repository>>"
            elif "Entity" in c["annotations"]:
                stereo = " <<Entity>>"

            # Declaratie
            if c["kind"] == "interface":
                lines.append(f'  interface {c["name"]}{stereo} {{')
            elif c["kind"] == "enum":
                lines.append(f'  enum {c["name"]}{stereo} {{')
            else:
                abstract = "abstract " if "abstract" in c["kind"] else ""
                lines.append(f'  {abstract}class {c["name"]}{stereo} {{')

            # Campuri (doar cele interesante - fara getters boilerplate)
            for ftype, fname in c["fields"][:10]:  # max 10 campuri
                lines.append(f"    -{fname} : {ftype}")

            # Metode publice
            for rtype, mname in c["methods"][:8]:  # max 8 metode
                if mname not in ("get", "set", "is", "toString", "hashCode", "equals"):
                    lines.append(f"    +{mname}() : {rtype}")

            lines.append("  }")
        if pkg:
            lines.append("}")
        lines.append("")

    # Relatii
    lines.append("' --- Relatii ---")
    for c in classes:
        if c["extends"] and c["extends"] in known_names:
            lines.append(f'{c["name"]} --|> {c["extends"]}')
        for iface in c["implements"]:
            if iface in known_names:
                lines.append(f'{c["name"]} ..|> {iface}')
        # Dependinte prin imports
        for imp in c["imports"]:
            short = imp.split(".")[-1]
            if short in known_names and short != c["name"]:
                lines.append(f'{c["name"]} --> {short}')

    lines += ["", "@enduml"]

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"Gata! {len(classes)} clase → {output_path}")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Utilizare: python java2puml.py <director_sursa> <output.puml>")
        sys.exit(1)

    generate_puml(sys.argv[1], sys.argv[2])

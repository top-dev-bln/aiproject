#!/usr/bin/env python3
"""
Convertor algoritmic PlantUML -> Mermaid (fara LLM).
Folosit pentru comparatia Static Analysis vs LLM in evaluare.

Utilizare:
  python puml2mermaid.py diagrame_puml/spring-boot-jwt.puml
  python puml2mermaid.py diagrame_puml/  (proceseaza tot folderul)
"""

import os
import re
import sys

# ── Clasificare layere ────────────────────────────────────────────────────────

STEREOTYPE_MAP = {
    "Controller": "Controllers",
    "Service":    "Services",
    "Repository": "Data_Access",
    "Entity":     "Data_Access",
}

SECURITY_KEYWORDS = [
    "security", "filter", "jwt", "token", "auth", "permission",
    "access", "firewall", "cors", "csrf", "password", "encoder",
]

INFRA_KEYWORDS = [
    "config", "configuration", "autoconfiguration", "properties",
    "scheduler", "cache", "messaging", "queue", "listener", "consumer",
    "producer", "gateway", "proxy", "swagger", "openapi",
]

# Filtre — aceste clase se exclud
EXCLUDE_SUFFIXES = [
    "dto", "request", "response", "vo", "form", "payload",
    "exception", "error", "constant", "constants", "util", "utils",
    "helper", "test", "tests", "spec", "mock", "stub",
    "application", "app", "initializer", "bootstrap", "main",
]

EXCLUDE_CONTAINS = ["test", "mock", "stub"]


def classify(name: str, stereotype: str) -> str | None:
    """Returneaza layer-ul clasei sau None daca trebuie exclusa."""
    lower = name.lower()

    # Excludere explicita
    for suffix in EXCLUDE_SUFFIXES:
        if lower.endswith(suffix):
            return None
    for word in EXCLUDE_CONTAINS:
        if word in lower:
            return None

    # Clasificare dupa stereotip explicit
    if stereotype in STEREOTYPE_MAP:
        return STEREOTYPE_MAP[stereotype]

    # Inferenta dupa nume — Security
    for kw in SECURITY_KEYWORDS:
        if kw in lower:
            return "Security"

    # Inferenta dupa nume — Infrastructure
    for kw in INFRA_KEYWORDS:
        if kw in lower:
            return "Infrastructure"

    # Repository fara stereotip (interfata cu Repository in nume)
    if "repository" in lower or "repo" in lower:
        return "Data_Access"

    # Controller fara stereotip
    if "controller" in lower or "endpoint" in lower or "resource" in lower:
        return "Controllers"

    # Service fara stereotip
    if "service" in lower or "manager" in lower or "facade" in lower:
        return "Services"

    return None  # ignora clasele neclasificabile


def parse_puml(content: str) -> tuple[dict, list]:
    """
    Parseaza PlantUML si returneaza:
      - classes: {name -> layer}
      - relations: [(from_name, to_name)]
    """
    classes = {}
    relations = []

    # Detecteaza clase/interfete/enum-uri cu sau fara stereotip
    class_pattern = re.compile(
        r"(?:class|interface|enum)\s+(\w+)(?:\s+<<(\w+)>>)?"
    )
    for match in class_pattern.finditer(content):
        name       = match.group(1)
        stereotype = match.group(2) or ""
        layer = classify(name, stereotype)
        if layer:
            classes[name] = layer

    # Detecteaza relatii
    rel_pattern = re.compile(r"(\w+)\s+--[>|]\s+(\w+)")
    for match in rel_pattern.finditer(content):
        src, dst = match.group(1), match.group(2)
        if src in classes and dst in classes:
            relations.append((src, dst))

    return classes, relations


def build_mermaid(classes: dict, relations: list) -> str:
    """Construieste diagrama Mermaid din classes si relations."""

    LAYER_ORDER = ["Controllers", "Services", "Data_Access", "Security", "Infrastructure"]

    # Grupeaza clasele pe layere
    layers: dict[str, list] = {l: [] for l in LAYER_ORDER}
    for name, layer in classes.items():
        if layer in layers:
            layers[layer].append(name)

    # Determina sagetile intre layere (deduplicat)
    layer_arrows = set()
    for src, dst in relations:
        src_layer = classes.get(src)
        dst_layer = classes.get(dst)
        if src_layer and dst_layer and src_layer != dst_layer:
            layer_arrows.add((src_layer, dst_layer))

    # Fallback: daca nu exista relatii explicite, adauga cele logice
    if not layer_arrows:
        existing = set(layers.keys())
        defaults = [
            ("Controllers", "Services"),
            ("Services",    "Data_Access"),
            ("Security",    "Controllers"),
            ("Infrastructure", "Services"),
        ]
        for src, dst in defaults:
            if src in existing and dst in existing:
                if layers[src] and layers[dst]:
                    layer_arrows.add((src, dst))

    lines = ["graph TB"]

    # Subgraph-uri
    for layer in LAYER_ORDER:
        if not layers[layer]:
            continue
        lines.append(f"  subgraph {layer}")
        for cls in sorted(layers[layer]):
            lines.append(f"    {cls}")
        lines.append("  end")

    # Sageti intre layere
    for src, dst in sorted(layer_arrows):
        lines.append(f"  {src} --> {dst}")

    return "\n".join(lines)


def convert_file(puml_path: str) -> str:
    with open(puml_path, "r", encoding="utf-8", errors="replace") as f:
        content = f.read()
    classes, relations = parse_puml(content)
    if not classes:
        return ""
    return build_mermaid(classes, relations)


def main():
    if len(sys.argv) < 2:
        print("Utilizare: python puml2mermaid.py <fisier.puml | director/>")
        sys.exit(1)

    target = sys.argv[1]

    if os.path.isfile(target):
        result = convert_file(target)
        print(result)

    elif os.path.isdir(target):
        out_dir = target.rstrip("/") + "_mermaid"
        os.makedirs(out_dir, exist_ok=True)
        ok = err = 0
        for fname in sorted(os.listdir(target)):
            if not fname.endswith(".puml"):
                continue
            result = convert_file(os.path.join(target, fname))
            if result:
                out_path = os.path.join(out_dir, fname.replace(".puml", ".mermaid"))
                with open(out_path, "w", encoding="utf-8") as f:
                    f.write(result)
                ok += 1
            else:
                err += 1
        print(f"Gata: {ok} convertite, {err} fara clase detectate → {out_dir}/")
    else:
        print(f"Eroare: '{target}' nu exista.")
        sys.exit(1)


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Analiza statica a codului Java pentru generarea diagramelor de arhitectura.

Modul central de analiza — importat de generate_diagrams_pr.py si git hook.
Nu are dependente externe in afara de java2puml.py (stdlib only).
"""

import os
import sys
from dataclasses import dataclass, field
from pathlib import Path

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from java2puml import parse_java_file


@dataclass
class DiagramResult:
    """
    Rezultatul analizei statice.
    Consumat de PR bot, git hook si model_backend pentru rafinare LLM.
    """
    mermaid: str
    plantuml: str
    classes: list          # dicts returnate de parse_java_file
    relations: list        # (from_class, rel_type, to_class) tuples
    source_files: list     # cai absolute ale fisierelor analizate
    changed_names: set     # numele claselor din fisierele modificate

    @property
    def changed_classes(self) -> list:
        return [c for c in self.classes if c["name"] in self.changed_names]

    @property
    def stats(self) -> dict:
        return {
            "total":        len(self.classes),
            "controllers":  len([c for c in self.classes if any(a in c["annotations"] for a in ["RestController", "Controller"])]),
            "services":     len([c for c in self.classes if "Service" in c["annotations"]]),
            "repositories": len([c for c in self.classes if "Repository" in c["annotations"]]),
            "entities":     len([c for c in self.classes if "Entity" in c["annotations"]]),
            "relations":    len(self.relations),
        }

    def is_empty(self) -> bool:
        return not self.classes


EMPTY_RESULT = DiagramResult(
    mermaid="", plantuml="", classes=[], relations=[],
    source_files=[], changed_names=set()
)

# ---------------------------------------------------------------------------
# I/O helpers
# ---------------------------------------------------------------------------

def load_changed_java_files(changed_files_path: str) -> list:
    """
    Citeste fisierele modificate si returneaza tupluri (display_path, abs_path)
    pentru fisierele .java existente pe disk.
    display_path = calea relativa originala (pentru afisare in PR comment)
    abs_path     = calea absoluta normalizata (pentru deduplicare cross-platform)
    """
    with open(changed_files_path, encoding="utf-8") as f:
        all_files = [line.strip() for line in f if line.strip()]
    result = []
    for f in all_files:
        if f.endswith(".java") and os.path.exists(f):
            result.append((f, str(Path(f).resolve())))
    return result


def collect_context_files(changed_files: list) -> list:
    """
    Colecteaza fisierele Java din acelasi director ca fisierele modificate.
    Ofera context complet pentru detectarea relatiilor dintre clase.
    Returneaza cai absolute (deduplicate corect pe Windows si Linux).
    """
    abs_paths = [abs_p for _, abs_p in changed_files]
    dirs = {os.path.dirname(p) for p in abs_paths}
    all_java = set(abs_paths)
    for d in dirs:
        if os.path.isdir(d):
            for fname in os.listdir(d):
                if fname.endswith(".java"):
                    all_java.add(str(Path(os.path.join(d, fname)).resolve()))
    return list(all_java)


# ---------------------------------------------------------------------------
# Parsing
# ---------------------------------------------------------------------------

def parse_files(java_files: list) -> list:
    """Parseaza fisierele Java si returneaza structurile de clase extrase."""
    classes = []
    for fpath in java_files:
        try:
            result = parse_java_file(fpath)
            if result:
                result["_source_file"] = fpath
                classes.append(result)
        except Exception as e:
            print(f"  [skip] {os.path.basename(fpath)}: {e}", file=sys.stderr)
    return classes


def extract_relations(classes: list) -> list:
    """
    Extrage relatiile dintre clase ca tupluri (from_class, rel_type, to_class).
    rel_type: "extends" | "implements" | "uses"
    """
    known_names = {c["name"] for c in classes}
    relations = []
    seen = set()

    for c in classes:
        if c["extends"] and c["extends"] in known_names:
            rel = (c["name"], "extends", c["extends"])
            if rel not in seen:
                relations.append(rel)
                seen.add(rel)

        for iface in c["implements"]:
            if iface in known_names:
                rel = (c["name"], "implements", iface)
                if rel not in seen:
                    relations.append(rel)
                    seen.add(rel)

        for imp in c["imports"]:
            short = imp.split(".")[-1]
            if short in known_names and short != c["name"]:
                rel = (c["name"], "uses", short)
                if rel not in seen:
                    relations.append(rel)
                    seen.add(rel)

    return relations


# ---------------------------------------------------------------------------
# Diagram generators
# ---------------------------------------------------------------------------

_STEREO_MAP_MERMAID = [
    ("RestController", "Controller"),
    ("Controller",     "Controller"),
    ("Service",        "Service"),
    ("Repository",     "Repository"),
    ("Entity",         "Entity"),
    ("Component",      "Component"),
    ("Configuration",  "Config"),
]

_STEREO_MAP_PUML = [
    ("RestController", "<<Controller>>"),
    ("Controller",     "<<Controller>>"),
    ("Service",        "<<Service>>"),
    ("Repository",     "<<Repository>>"),
    ("Entity",         "<<Entity>>"),
    ("Component",      "<<Component>>"),
    ("Configuration",  "<<Config>>"),
]

_SKIP_METHODS = {"get", "set", "is", "toString", "hashCode", "equals"}

_REL_MERMAID = {"extends": "--|>", "implements": "..|>", "uses": "-->"}
_REL_PUML    = {"extends": "--|>", "implements": "..|>", "uses": "-->"}


def to_mermaid(classes: list, relations: list) -> str:
    """Converteste clasele si relatiile in sintaxa Mermaid classDiagram."""
    if not classes:
        return ""

    lines = ["classDiagram"]

    for c in classes:
        stereo = ""
        for ann, label in _STEREO_MAP_MERMAID:
            if ann in c["annotations"]:
                stereo = f"<<{label}>>"
                break
        if not stereo and c["kind"] == "interface":
            stereo = "<<interface>>"
        elif not stereo and c["kind"] == "enum":
            stereo = "<<enum>>"

        header = f"    class {c['name']}"
        if stereo:
            header += f" {stereo}"
        lines.append(header + " {")

        for ftype, fname in c["fields"][:6]:
            lines.append(f"        -{fname} {ftype}")

        count = 0
        for rtype, mname in c["methods"]:
            if count >= 5:
                break
            if mname in _SKIP_METHODS:
                continue
            lines.append(f"        +{mname}() {rtype}")
            count += 1

        lines.append("    }")

    if relations:
        lines.append("")
        for from_c, rel_type, to_c in relations:
            arrow = _REL_MERMAID.get(rel_type, "-->")
            lines.append(f"    {from_c} {arrow} {to_c}")

    return "\n".join(lines)


def to_plantuml_string(classes: list, relations: list) -> str:
    """Converteste clasele si relatiile in text PlantUML."""
    if not classes:
        return ""

    lines = ["@startuml", "skinparam classAttributeIconSize 0", ""]

    packages = {}
    for c in classes:
        packages.setdefault(c["package"] or "(default)", []).append(c)

    for pkg, members in sorted(packages.items()):
        if pkg and pkg != "(default)":
            lines.append(f"package {pkg} {{")
        for c in members:
            stereo = ""
            for ann, label in _STEREO_MAP_PUML:
                if ann in c["annotations"]:
                    stereo = f" {label}"
                    break

            if c["kind"] == "interface":
                lines.append(f"  interface {c['name']}{stereo} {{")
            elif c["kind"] == "enum":
                lines.append(f"  enum {c['name']}{stereo} {{")
            else:
                lines.append(f"  class {c['name']}{stereo} {{")

            for ftype, fname in c["fields"][:8]:
                lines.append(f"    -{fname} : {ftype}")

            count = 0
            for rtype, mname in c["methods"]:
                if count >= 6:
                    break
                if mname in _SKIP_METHODS:
                    continue
                lines.append(f"    +{mname}() : {rtype}")
                count += 1

            lines.append("  }")
        if pkg and pkg != "(default)":
            lines.append("}")
        lines.append("")

    lines.append("' --- Relatii ---")
    for from_c, rel_type, to_c in relations:
        arrow = _REL_PUML.get(rel_type, "-->")
        lines.append(f"{from_c} {arrow} {to_c}")

    lines += ["", "@enduml"]
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# Main entry point
# ---------------------------------------------------------------------------

def analyze(changed_files_path: str, include_context: bool = True) -> DiagramResult:
    """
    Punct de intrare principal.
    Analizeaza fisierele modificate si returneaza un DiagramResult complet.
    """
    changed_files = load_changed_java_files(changed_files_path)
    if not changed_files:
        return EMPTY_RESULT

    all_abs = (
        collect_context_files(changed_files)
        if include_context
        else [abs_p for _, abs_p in changed_files]
    )

    classes   = parse_files(all_abs)
    relations = extract_relations(classes)
    mermaid   = to_mermaid(classes, relations)
    plantuml  = to_plantuml_string(classes, relations)

    changed_names = set()
    for _, abs_p in changed_files:
        try:
            r = parse_java_file(abs_p)
            if r:
                changed_names.add(r["name"])
        except Exception:
            pass

    return DiagramResult(
        mermaid=mermaid,
        plantuml=plantuml,
        classes=classes,
        relations=relations,
        source_files=all_abs,
        changed_names=changed_names,
    )

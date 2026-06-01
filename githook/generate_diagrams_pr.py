#!/usr/bin/env python3
"""
Generează diagrame de arhitectură pentru PR-uri GitHub.
Analizează fișierele Java modificate și produce un comentariu
cu diagrama Mermaid și PlantUML pentru postare pe Pull Request.

Utilizare:
    python generate_diagrams_pr.py --changed-files changed_files.txt --output pr_comment.md
"""

import os
import sys
import argparse
from pathlib import Path

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from java2puml import parse_java_file


def load_changed_java_files(changed_files_path: str) -> list:
    """
    Citește fișierele modificate și returnează tupluri (display_path, abs_path)
    pentru fișierele .java existente.
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
    Colectează fișierele Java din același director ca fișierele modificate.
    Returnează căi absolute pentru deduplicare corectă cross-platform.
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


def parse_files(java_files: list) -> list:
    """Parsează fișierele Java și returnează structurile extrase."""
    classes = []
    for fpath in java_files:
        try:
            result = parse_java_file(fpath)
            if result:
                result["_source_file"] = fpath
                classes.append(result)
        except Exception as e:
            print(f"  [skip] {fpath}: {e}", file=sys.stderr)
    return classes


def to_mermaid(classes: list) -> str:
    """Convertește clasele parsate în sintaxă Mermaid classDiagram."""
    if not classes:
        return ""

    known_names = {c["name"] for c in classes}

    STEREO_MAP = [
        ("RestController", "Controller"),
        ("Controller", "Controller"),
        ("Service", "Service"),
        ("Repository", "Repository"),
        ("Entity", "Entity"),
        ("Component", "Component"),
        ("Configuration", "Config"),
    ]

    SKIP_METHODS = {"get", "set", "is", "toString", "hashCode", "equals"}

    lines = ["classDiagram"]

    for c in classes:
        stereo = ""
        for ann, label in STEREO_MAP:
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
            if mname in SKIP_METHODS:
                continue
            lines.append(f"        +{mname}() {rtype}")
            count += 1

        lines.append("    }")

    lines.append("")

    seen_relations = set()
    for c in classes:
        if c["extends"] and c["extends"] in known_names:
            rel = f"    {c['name']} --|> {c['extends']}"
            if rel not in seen_relations:
                lines.append(rel)
                seen_relations.add(rel)
        for iface in c["implements"]:
            if iface in known_names:
                rel = f"    {c['name']} ..|> {iface}"
                if rel not in seen_relations:
                    lines.append(rel)
                    seen_relations.add(rel)
        for imp in c["imports"]:
            short = imp.split(".")[-1]
            if short in known_names and short != c["name"]:
                rel = f"    {c['name']} --> {short}"
                if rel not in seen_relations:
                    lines.append(rel)
                    seen_relations.add(rel)

    return "\n".join(lines)


def to_plantuml_string(classes: list) -> str:
    """Convertește clasele parsate în text PlantUML."""
    if not classes:
        return ""

    known_names = {c["name"] for c in classes}

    STEREO_MAP = [
        ("RestController", "<<Controller>>"),
        ("Controller", "<<Controller>>"),
        ("Service", "<<Service>>"),
        ("Repository", "<<Repository>>"),
        ("Entity", "<<Entity>>"),
        ("Component", "<<Component>>"),
        ("Configuration", "<<Config>>"),
    ]

    SKIP_METHODS = {"get", "set", "is", "toString", "hashCode", "equals"}

    lines = ["@startuml", "skinparam classAttributeIconSize 0", ""]

    packages = {}
    for c in classes:
        packages.setdefault(c["package"] or "(default)", []).append(c)

    for pkg, members in sorted(packages.items()):
        if pkg and pkg != "(default)":
            lines.append(f"package {pkg} {{")
        for c in members:
            stereo = ""
            for ann, label in STEREO_MAP:
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
                if mname in SKIP_METHODS:
                    continue
                lines.append(f"    +{mname}() : {rtype}")
                count += 1

            lines.append("  }")
        if pkg and pkg != "(default)":
            lines.append("}")
        lines.append("")

    lines.append("' --- Relații ---")
    seen = set()
    for c in classes:
        if c["extends"] and c["extends"] in known_names:
            rel = f"{c['name']} --|> {c['extends']}"
            if rel not in seen:
                lines.append(rel)
                seen.add(rel)
        for iface in c["implements"]:
            if iface in known_names:
                rel = f"{c['name']} ..|> {iface}"
                if rel not in seen:
                    lines.append(rel)
                    seen.add(rel)
        for imp in c["imports"]:
            short = imp.split(".")[-1]
            if short in known_names and short != c["name"]:
                rel = f"{c['name']} --> {short}"
                if rel not in seen:
                    lines.append(rel)
                    seen.add(rel)

    lines += ["", "@enduml"]
    return "\n".join(lines)


def build_stats_line(all_classes: list) -> str:
    """Construiește un rezumat al componentelor detectate."""
    controllers = [c for c in all_classes if any(a in c["annotations"] for a in ["RestController", "Controller"])]
    services    = [c for c in all_classes if "Service" in c["annotations"]]
    repos       = [c for c in all_classes if "Repository" in c["annotations"]]
    entities    = [c for c in all_classes if "Entity" in c["annotations"]]

    parts = []
    if controllers:
        parts.append(f"**{len(controllers)}** Controller{'s' if len(controllers) > 1 else ''}")
    if services:
        parts.append(f"**{len(services)}** Service{'s' if len(services) > 1 else ''}")
    if repos:
        parts.append(f"**{len(repos)}** Repositor{'ies' if len(repos) > 1 else 'y'}")
    if entities:
        parts.append(f"**{len(entities)}** Entit{'ies' if len(entities) > 1 else 'y'}")

    base = " · ".join(parts) if parts else ""
    total = f"**{len(all_classes)}** clase total"
    return f"{base} · {total}" if base else total


def format_pr_comment(
    mermaid: str,
    plantuml: str,
    changed_java: list,
    all_classes: list,
    changed_names: set,
) -> str:
    """Formatează comentariul complet pentru postare pe PR."""

    MARKER = "<!-- architecture-diagram-bot -->"

    changed_list = "\n".join(f"- `{disp}`" for disp, _ in sorted(changed_java))
    stats = build_stats_line(all_classes)

    highlight_line = ""
    if changed_names:
        names_fmt = ", ".join(f"`{n}`" for n in sorted(changed_names))
        highlight_line = f"\n**Clase modificate:** {names_fmt}\n"

    return f"""{MARKER}
## Architecture Diagram Update

**Fisiere Java modificate ({len(changed_java)}):**
{changed_list}
{highlight_line}
**Componente detectate:** {stats}

---

### Diagrama Mermaid

```mermaid
{mermaid}
```

<details>
<summary>PlantUML (click pentru expandare)</summary>

```plantuml
{plantuml}
```

</details>

---
> Generata automat de **Architecture Diagram Bot** · powered by [java2puml](./java2puml.py)
"""


def main():
    parser = argparse.ArgumentParser(
        description="Generează diagrame de arhitectură pentru comentariu PR"
    )
    parser.add_argument(
        "--changed-files", required=True,
        help="Fișier text cu lista fișierelor modificate (unul per linie)"
    )
    parser.add_argument(
        "--output", required=True,
        help="Fișier de output pentru comentariul formatat"
    )
    parser.add_argument(
        "--no-context", action="store_true",
        help="Nu include fișierele din același director (context mai mic)"
    )
    args = parser.parse_args()

    print("Citesc fișierele modificate...", file=sys.stderr)
    # changed_java: list of (display_path, abs_path)
    changed_java = load_changed_java_files(args.changed_files)

    if not changed_java:
        print("Niciun fisier .java modificat. Sar peste generarea diagramei.", file=sys.stderr)
        Path(args.output).write_text("<!-- no-java-changes -->", encoding="utf-8")
        sys.exit(0)

    print(f"  Fisiere Java modificate: {len(changed_java)}", file=sys.stderr)

    if args.no_context:
        all_files = [abs_p for _, abs_p in changed_java]
    else:
        all_files = collect_context_files(changed_java)
        print(f"  Total fisiere cu context: {len(all_files)}", file=sys.stderr)

    print("Parsez fisierele Java...", file=sys.stderr)
    all_classes = parse_files(all_files)
    print(f"  Clase detectate: {len(all_classes)}", file=sys.stderr)

    if not all_classes:
        print("Nu s-au putut parsa clasele Java.", file=sys.stderr)
        Path(args.output).write_text("<!-- parse-failed -->", encoding="utf-8")
        sys.exit(0)

    # Identifică exact clasele din fișierele modificate (după abs_path)
    changed_names = set()
    for _, abs_p in changed_java:
        try:
            r = parse_java_file(abs_p)
            if r:
                changed_names.add(r["name"])
        except Exception:
            pass

    print("Generez diagrama Mermaid...", file=sys.stderr)
    mermaid = to_mermaid(all_classes)

    print("Generez diagrama PlantUML...", file=sys.stderr)
    plantuml = to_plantuml_string(all_classes)

    comment = format_pr_comment(mermaid, plantuml, changed_java, all_classes, changed_names)
    Path(args.output).write_text(comment, encoding="utf-8")
    print(f"Comment scris in: {args.output}", file=sys.stderr)


if __name__ == "__main__":
    main()

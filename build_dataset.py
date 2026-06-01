#!/usr/bin/env python3
"""
Construieste dataset-ul de training in format Hugging Face / Unsloth (JSONL messages).
Genereaza 3 sample-uri per proiect:
  1. free_text_description -> mermaid_diagram
  2. user_stories          -> mermaid_diagram
  3. puml_content          -> mermaid_diagram

Output:
  dataset/train.jsonl  (80%)
  dataset/val.jsonl    (20%)

Utilizare: python build_dataset.py
"""

import os
import json
import random

PUML_DIR    = "diagrame_puml"
JSON_DIR    = "json_output"
OUT_DIR     = "dataset"
TRAIN_SPLIT = 0.8
SEED        = 42

SYSTEM_PROMPT = (
    "You are an expert software architecture assistant specialized in generating Mermaid architecture diagrams from Java Spring Boot projects."
"You are acting as an expert AI tool that creates Mermaid diagrams specifically for Spring Boot applications."

"You accept three types of input:"
"You can process three specific formats of source information."

"A plain-text description of the system's architecture and business domain"
"Input type 1: A standard text explanation of how the system is built and its business logic."

"A list of user stories describing the system's features and actors"
"Input type 2: Requirements written as user stories that define roles and features."

"A PlantUML class diagram extracted via static analysis of the source code"
"Input type 3: A class diagram generated automatically from the actual Java code."

"Your output is ALWAYS a single valid Mermaid diagram in graph TB layout. Nothing else — no explanation, no markdown fences, no commentary."
"The response must strictly contain the raw Mermaid code structured from top to bottom, with absolutely no text before or after it."

"Diagram rules:"
"These are the constraints for building the diagram:"

"Group components into subgraphs. Use only the subgraphs that apply: Controllers, Services, Data_Access, Security, Infrastructure."
"Organize classes into specific visual boundaries representing standard architectural layers."

"Include only architecturally significant components: classes annotated as Controller, Service, Repository, Entity, and security-related filters or configurations."
"Filter out minor details and keep only core Spring components and database models."

"Exclude: DTOs, POJOs, value objects, exceptions, constants, test classes, and application boot/initializer classes."
"Ignore data transfer objects, utilities, errors, and configuration files that don't impact the high-level architecture."

"Draw dependency arrows between subgraphs only, not between individual classes."
"Connections/arrows should only show how layers interact with each other, rather than linking class to class."

"Omit any subgraph that would contain zero components after filtering."
"If a layer (like Security) has no relevant classes, do not include that empty layer in the diagram."

"Output format:"
"The exact structural template the response must follow:"

"graph TB"
"Defines a top-to-bottom flowchart layout."

"  subgraph Controllers"
"    ..."
"  end"
"Creates a visual container for the API/Web layer components."

"  subgraph Services"
"    ..."
"  end"
"Creates a visual container for the business logic components."

"  Controllers --> Services"
"Shows that the controller layer depends on and calls the service layer."

"  ..."
"Placeholder for other layer connections (e.g., Services --> Data_Access)."
)


def make_sample(user_content: str, mermaid: str) -> dict:
    return {
        "messages": [
            {"role": "system",    "content": SYSTEM_PROMPT},
            {"role": "user",      "content": user_content},
            {"role": "assistant", "content": mermaid},
        ]
    }


def user_stories_to_text(stories: list) -> str:
    lines = ["User stories:"]
    for s in stories:
        lines.append(f"- {s['story']}")
        if s.get("services"):
            lines.append(f"  Services: {', '.join(s['services'])}")
        if s.get("controllers"):
            lines.append(f"  Controllers: {', '.join(s['controllers'])}")
    return "\n".join(lines)


def main():
    # Gaseste perechile json <-> puml
    json_names = {
        os.path.splitext(f)[0]
        for f in os.listdir(JSON_DIR) if f.endswith(".json")
    }
    puml_names = {
        os.path.splitext(f)[0]
        for f in os.listdir(PUML_DIR) if f.endswith(".puml")
    }
    common = sorted(json_names & puml_names)

    missing = (json_names ^ puml_names)
    if missing:
        print(f"Atentie: {len(missing)} fisiere fara pereche (ignorate). Ruleaza sync_folders.py.")

    print(f"Proiecte gasite: {len(common)}")

    # Construieste sample-urile
    samples = []
    erori   = 0

    for name in common:
        try:
            with open(os.path.join(JSON_DIR,  f"{name}.json"), "r", encoding="utf-8") as f:
                data = json.load(f)
            with open(os.path.join(PUML_DIR, f"{name}.puml"),  "r", encoding="utf-8", errors="replace") as f:
                puml = f.read()

            mermaid      = data["output"]["mermaid_diagram"]
            free_text    = data["input"]["free_text_description"]
            user_stories = data["input"]["user_stories"]

            # Sample 1: free text -> mermaid
            samples.append(make_sample(
                f"Generate a Mermaid architecture diagram for this system:\n\n{free_text}",
                mermaid
            ))

            # Sample 2: user stories -> mermaid
            samples.append(make_sample(
                f"Generate a Mermaid architecture diagram based on these user stories:\n\n{user_stories_to_text(user_stories)}",
                mermaid
            ))

            # Sample 3: puml -> mermaid
            samples.append(make_sample(
                f"Convert this PlantUML class diagram into a Mermaid architecture diagram:\n\n{puml}",
                mermaid
            ))

        except Exception as e:
            print(f"  Eroare la '{name}': {e}")
            erori += 1

    print(f"Samples construite: {len(samples)}  ({erori} erori, {len(common) - erori} proiecte x 3)")

    # Shuffle la nivel de proiect (grupuri de 3) ca sa nu separam sample-urile aceluiasi proiect
    groups = [samples[i:i+3] for i in range(0, len(samples), 3)]
    random.seed(SEED)
    random.shuffle(groups)

    split_idx    = int(len(groups) * TRAIN_SPLIT)
    train_groups = groups[:split_idx]
    val_groups   = groups[split_idx:]

    train_samples = [s for g in train_groups for s in g]
    val_samples   = [s for g in val_groups   for s in g]

    # Scrie fisierele
    os.makedirs(OUT_DIR, exist_ok=True)

    def write_jsonl(path, data):
        with open(path, "w", encoding="utf-8") as f:
            for item in data:
                f.write(json.dumps(item, ensure_ascii=False) + "\n")
        print(f"  {path}  ({len(data)} samples)")

    write_jsonl(os.path.join(OUT_DIR, "train.jsonl"), train_samples)
    write_jsonl(os.path.join(OUT_DIR, "val.jsonl"),   val_samples)

    print(f"\nDataset final:")
    print(f"  Train : {len(train_samples)} samples  ({len(train_groups)} proiecte)")
    print(f"  Val   : {len(val_samples)} samples  ({len(val_groups)} proiecte)")
    print(f"  Total : {len(samples)} samples")


if __name__ == "__main__":
    main()

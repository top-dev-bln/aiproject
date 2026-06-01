#!/usr/bin/env python3
"""
Genereaza JSON-uri de training din fisiere .puml folosind Claude API.
Utilizare: python genereaza_json.py --api-key sk-ant-...
           python genereaza_json.py --api-key sk-ant-... --limit 10
           python genereaza_json.py --api-key sk-ant-... --file spring-boot-jwt.puml
"""

import os
import json
import time
import argparse
import anthropic

PUML_DIR = "diagrame_puml"
JSON_DIR = "json_training"

SYSTEM_PROMPT = """You are a Senior Software Architect specialising in reverse-engineering Java Spring Boot codebases from static-analysis PlantUML diagrams. You always respond with ONLY a valid JSON object – no markdown fences, no commentary.

The JSON must follow exactly this schema:
{
  "input": {
    "free_text_description": "...",
    "user_stories": [
      {
        "id": 1,
        "story": "As a [Role], I want to [Action], so that [Value].",
        "services": ["ServiceA"],
        "controllers": ["ControllerA"]
      }
    ]
  },
  "output": {
    "mermaid_diagram": "graph TB ..."
  }
}

Rules for free_text_description:
- Write 3–5 sentences in plain English.
- Describe WHAT the system does from a business perspective, not HOW it is implemented.
- Identify the primary domain (e.g. authentication, e-commerce, IoT, messaging).
- Name the key actors or user roles that interact with the system.
- Describe the 2–3 main business workflows (e.g. registration, order placement, sensor ingestion).
- Do NOT mention framework names (Spring, Hibernate, JWT, BCrypt, etc.) — describe behavior only.
- Infer meaning from class names, stereotypes (<<Controller>>, <<Service>>, <<Entity>>), and relationships.

Rules for user_stories:
- Identify the 5 most important BUSINESS features (not technical plumbing).
- Each story follows strictly "As a [Role], I want to [Action], so that [Value]."
- List only concrete service and controller class names that actually appear in the PlantUML.
- Roles should be domain actors (User, Admin, Customer, Developer, etc.), not technical roles.

Rules for mermaid_diagram:
- Use graph TB layout.
- Group classes into subgraphs: Controllers, Services, Data_Access, Security, Infrastructure.
  Only include subgraphs that have at least one relevant class.
- Filter OUT: DTOs, POJOs, VOs, Exceptions, Constants, Test classes,
  classes ending in *Application / *Initializer / *App / *Config (unless they are Security configs).
- Keep only: <<Controller>>, <<Service>>, <<Repository>>, <<Entity>>, security filters, security configs.
- Show dependency arrows between SUBGRAPHS only (not individual class-to-class arrows).
- Use meaningful subgraph labels (e.g. Controllers, Services, Data_Access, Security).
- If a subgraph would have 0 classes after filtering, omit it entirely."""


def build_user_message(puml_content: str, project_name: str) -> str:
    return f"""Project name: {project_name}

PlantUML diagram:
{puml_content}

Generate the JSON for this project."""


def generate_json_for_puml(client: anthropic.Anthropic, puml_path: str) -> dict:
    project_name = os.path.splitext(os.path.basename(puml_path))[0]

    with open(puml_path, "r", encoding="utf-8", errors="replace") as f:
        puml_content = f.read()

    message = client.messages.create(
        model="claude-opus-4-6",
        max_tokens=2048,
        system=SYSTEM_PROMPT,
        messages=[
            {"role": "user", "content": build_user_message(puml_content, project_name)}
        ],
    )

    raw = message.content[0].text.strip()

    # Curata markdown fences daca Claude le adauga totusi
    if raw.startswith("```"):
        raw = raw.split("```")[1]
        if raw.startswith("json"):
            raw = raw[4:]
        raw = raw.strip()

    result = json.loads(raw)

    # Injecteaza metadata
    result["metadata"] = {
        "project_name": project_name,
        "puml_source": os.path.basename(puml_path),
    }

    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--api-key", required=True, help="Anthropic API key")
    parser.add_argument("--limit", type=int, default=None, help="Proceseaza maxim N fisiere")
    parser.add_argument("--file", default=None, help="Proceseaza un singur fisier .puml")
    parser.add_argument("--delay", type=float, default=1.0, help="Secunde intre request-uri (default 1.0)")
    args = parser.parse_args()

    client = anthropic.Anthropic(api_key=args.api_key)

    os.makedirs(JSON_DIR, exist_ok=True)

    # Selectie fisiere
    if args.file:
        puml_files = [os.path.join(PUML_DIR, args.file)]
    else:
        puml_files = sorted([
            os.path.join(PUML_DIR, f)
            for f in os.listdir(PUML_DIR)
            if f.endswith(".puml")
        ])
        if args.limit:
            puml_files = puml_files[:args.limit]

    total = len(puml_files)
    succes = 0
    erori = 0

    print(f"Procesez {total} fisiere...\n")

    for idx, puml_path in enumerate(puml_files, 1):
        project_name = os.path.splitext(os.path.basename(puml_path))[0]
        json_path = os.path.join(JSON_DIR, f"{project_name}.json")

        if os.path.exists(json_path):
            print(f"[{idx}/{total}] Skip '{project_name}' - exista deja.")
            continue

        print(f"[{idx}/{total}] Procesez: {project_name} ...", end=" ", flush=True)

        try:
            result = generate_json_for_puml(client, puml_path)

            with open(json_path, "w", encoding="utf-8") as f:
                json.dump(result, f, indent=2, ensure_ascii=False)

            print("OK")
            succes += 1

        except json.JSONDecodeError as e:
            print(f"EROARE JSON: {e}")
            erori += 1
        except anthropic.RateLimitError:
            print("RATE LIMIT - astept 30s...")
            time.sleep(30)
            erori += 1
        except Exception as e:
            print(f"EROARE: {e}")
            erori += 1

        if idx < total:
            time.sleep(args.delay)

    print(f"\nGata: {succes} succes, {erori} erori. JSON-uri in: {JSON_DIR}/")


if __name__ == "__main__":
    main()

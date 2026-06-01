#!/usr/bin/env python3
"""
Pipeline complet de evaluare pentru un proiect Java.

Pasi:
  1. java2puml   → genereaza PlantUML din codul sursa
  2. puml2mermaid → converteste static PlantUML → Mermaid
  3. API call     → trimite PlantUML la diagram-generator → Mermaid LLM
  4. evaluate     → compara ambele vs ground truth (daca exista)

Utilizare:
  python compare_project.py dataset_brut/auth-api
  python compare_project.py dataset_brut/auth-api --model qwen2.5-coder:7b
  python compare_project.py dataset_brut/auth-api --no-api  (doar static)
"""

import os
import sys
import json
import time
import argparse
import requests
import subprocess
import tempfile

API_URL      = "http://localhost:8000/diagrams/generate"
DEFAULT_MODEL = "qwen2.5-coder:14b"
GROUND_TRUTH_DIR = "json_output"


# ── Helpers ───────────────────────────────────────────────────────────────────

def run_java2puml(project_path: str, out_puml: str) -> bool:
    """Genereaza PlantUML din codul sursa folosind java2puml.py."""
    script = os.path.join(os.path.dirname(__file__), "java2puml.py")
    result = subprocess.run(
        [sys.executable, script, project_path, out_puml],
        capture_output=True, text=True
    )
    if result.returncode != 0:
        print(f"  [java2puml] Eroare: {result.stderr}")
        return False
    print(f"  [java2puml] {result.stdout.strip()}")
    return os.path.exists(out_puml)


def run_puml2mermaid(puml_path: str) -> str:
    """Converteste PlantUML → Mermaid static."""
    script = os.path.join(os.path.dirname(__file__), "puml2mermaid.py")
    result = subprocess.run(
        [sys.executable, script, puml_path],
        capture_output=True, text=True
    )
    return result.stdout.strip()


def call_api(puml_content: str, model: str) -> str | None:
    """Trimite PlantUML la diagram-generator API si returneaza Mermaid."""
    payload = {
        "prompt": f"Convert this PlantUML class diagram into a Mermaid architecture diagram:\n\n{puml_content}",
        "syntax_type": "mermaid",
        "subtype": "auto",
        "model": model,
        "options": {
            "agent": {"enabled": True, "max_iterations": 3}
        }
    }
    try:
        resp = requests.post(API_URL, json=payload, timeout=120)
        resp.raise_for_status()
        data = resp.json()
        return data.get("code") or data.get("diagram", {}).get("code")
    except requests.exceptions.ConnectionError:
        print("  [API] Eroare: backend-ul nu ruleaza. Porneste run.py mai intai.")
        return None
    except Exception as e:
        print(f"  [API] Eroare: {e}")
        return None


def load_ground_truth(project_name: str) -> str | None:
    path = os.path.join(GROUND_TRUTH_DIR, f"{project_name}.json")
    if not os.path.exists(path):
        return None
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)["output"]["mermaid_diagram"]


def parse_mermaid(content: str) -> tuple[dict, set]:
    import re
    node_layers: dict[str, str] = {}
    edges: set = set()
    current_subgraph = None
    for line in content.splitlines():
        line = line.strip()
        m = re.match(r"subgraph\s+(\w+)", line)
        if m:
            current_subgraph = m.group(1)
            continue
        if line == "end":
            current_subgraph = None
            continue
        if current_subgraph and line and "-->" not in line and not line.startswith("graph"):
            node = re.match(r"(\w+)", line)
            if node:
                node_layers[node.group(1)] = current_subgraph
        m = re.match(r"(\w+)\s*-->\s*(\w+)", line)
        if m:
            edges.add((m.group(1), m.group(2)))
    return node_layers, edges


def compute_metrics(pred_layers, pred_edges, gt_layers, gt_edges) -> dict:
    pred_nodes = set(pred_layers.keys())
    gt_nodes   = set(gt_layers.keys())
    tp_nodes   = pred_nodes & gt_nodes

    prec  = len(tp_nodes) / len(pred_nodes) if pred_nodes else 0.0
    rec   = len(tp_nodes) / len(gt_nodes)   if gt_nodes   else 0.0
    f1    = 2*prec*rec/(prec+rec) if prec+rec > 0 else 0.0
    layer = sum(1 for n in tp_nodes if pred_layers[n] == gt_layers[n]) / len(tp_nodes) if tp_nodes else 0.0
    tp_e  = pred_edges & gt_edges
    ep    = len(tp_e) / len(pred_edges) if pred_edges else 0.0
    er    = len(tp_e) / len(gt_edges)   if gt_edges   else 0.0
    ef1   = 2*ep*er/(ep+er) if ep+er > 0 else 0.0
    jacc  = len(tp_nodes) / len(pred_nodes | gt_nodes) if (pred_nodes | gt_nodes) else 0.0

    return {
        "node_precision": prec, "node_recall": rec, "node_f1": f1,
        "layer_accuracy": layer,
        "edge_precision": ep, "edge_recall": er, "edge_f1": ef1,
        "jaccard": jacc,
    }


def print_comparison(static_mermaid: str, llm_mermaid: str | None, gt_mermaid: str | None):
    print("\n" + "="*65)
    print("  STATIC (puml2mermaid.py)")
    print("="*65)
    print(static_mermaid or "  (gol)")

    if llm_mermaid:
        print("\n" + "="*65)
        print("  LLM (diagram-generator API)")
        print("="*65)
        print(llm_mermaid)

    if gt_mermaid:
        print("\n" + "="*65)
        print("  GROUND TRUTH")
        print("="*65)
        print(gt_mermaid)

        print("\n" + "="*65)
        print("  METRICI vs GROUND TRUTH")
        print("="*65)
        gt_l, gt_e = parse_mermaid(gt_mermaid)

        headers = ["Metric", "Static"]
        if llm_mermaid:
            headers.append("LLM")
        print(f"  {headers[0]:<22} {headers[1]:>10}" + (f"  {headers[2]:>10}" if llm_mermaid else ""))
        print(f"  {'-'*22} {'-'*10}" + (f"  {'-'*10}" if llm_mermaid else ""))

        s_l, s_e = parse_mermaid(static_mermaid) if static_mermaid else ({}, set())
        s_m = compute_metrics(s_l, s_e, gt_l, gt_e)

        l_m = None
        if llm_mermaid:
            ll_l, ll_e = parse_mermaid(llm_mermaid)
            l_m = compute_metrics(ll_l, ll_e, gt_l, gt_e)

        for key, label in [
            ("node_precision", "Node Precision"),
            ("node_recall",    "Node Recall"),
            ("node_f1",        "Node F1"),
            ("layer_accuracy", "Layer Accuracy"),
            ("edge_f1",        "Edge F1"),
            ("jaccard",        "Jaccard"),
        ]:
            row = f"  {label:<22} {s_m[key]:>10.3f}"
            if l_m:
                winner = " ✓" if l_m[key] > s_m[key] else ""
                row += f"  {l_m[key]:>10.3f}{winner}"
            print(row)
    else:
        print(f"\n  [Info] Nu exista ground truth pentru acest proiect in {GROUND_TRUTH_DIR}/")


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("project_path", help="Calea catre proiectul Java")
    parser.add_argument("--model",  default=DEFAULT_MODEL, help=f"Modelul Ollama (default: {DEFAULT_MODEL})")
    parser.add_argument("--no-api", action="store_true", help="Sari peste apelul API (doar static)")
    parser.add_argument("--save",   action="store_true", help="Salveaza outputurile in fisiere")
    args = parser.parse_args()

    project_name = os.path.basename(args.project_path.rstrip("/"))
    print(f"\nProiect: {project_name}")
    print(f"{'─'*50}")

    # Step 1: java2puml
    with tempfile.NamedTemporaryFile(suffix=".puml", delete=False) as tmp:
        puml_path = tmp.name

    print("1. Generez PlantUML...")
    if not run_java2puml(args.project_path, puml_path):
        print("  Eroare la generarea PUML. Verifica calea proiectului.")
        sys.exit(1)

    with open(puml_path, "r", encoding="utf-8") as f:
        puml_content = f.read()

    # Step 2: static conversion
    print("2. Conversie statica PUML → Mermaid...")
    static_mermaid = run_puml2mermaid(puml_path)
    print(f"  OK ({len(static_mermaid.splitlines())} linii)")

    # Step 3: LLM via API
    llm_mermaid = None
    if not args.no_api:
        print(f"3. Generare LLM via API (model: {args.model})...")
        start = time.time()
        llm_mermaid = call_api(puml_content, args.model)
        elapsed = time.time() - start
        if llm_mermaid:
            print(f"  OK ({len(llm_mermaid.splitlines())} linii, {elapsed:.1f}s)")
        else:
            print("  Esuat — continuam fara LLM")

    # Step 4: Ground truth + metrici
    print("4. Incarc ground truth...")
    gt_mermaid = load_ground_truth(project_name)
    print(f"  {'Gasit' if gt_mermaid else 'Nu exista'}")

    # Salvare
    if args.save:
        os.makedirs(f"comparatii/{project_name}", exist_ok=True)
        with open(f"comparatii/{project_name}/static.mermaid", "w") as f:
            f.write(static_mermaid)
        if llm_mermaid:
            with open(f"comparatii/{project_name}/llm.mermaid", "w") as f:
                f.write(llm_mermaid)
        if gt_mermaid:
            with open(f"comparatii/{project_name}/ground_truth.mermaid", "w") as f:
                f.write(gt_mermaid)
        print(f"  Salvat in comparatii/{project_name}/")

    # Afisare
    print_comparison(static_mermaid, llm_mermaid, gt_mermaid)

    os.unlink(puml_path)


if __name__ == "__main__":
    main()

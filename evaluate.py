#!/usr/bin/env python3
"""
Evalueaza calitatea diagramelor Mermaid generate fata de ground truth.
Compara: Static (puml2mermaid) vs LLM vs Ground Truth

Utilizare:
  python evaluate.py                         # compara static vs ground truth
  python evaluate.py --llm-dir llm_outputs/  # compara si LLM
  python evaluate.py --project spring-boot-jwt  # un singur proiect
"""

import os
import re
import json
import argparse
from dataclasses import dataclass, field

GROUND_TRUTH_DIR = "json_output"
STATIC_DIR       = "diagrame_puml_mermaid"


# ── Parser Mermaid ────────────────────────────────────────────────────────────

def parse_mermaid(content: str) -> tuple[dict[str, str], set[tuple]]:
    """
    Returneaza:
      - node_layers: {node_name -> subgraph_name}
      - edges: {(src_layer, dst_layer)}
    """
    node_layers: dict[str, str] = {}
    edges: set[tuple] = set()

    current_subgraph = None
    for line in content.splitlines():
        line = line.strip()

        # Intra in subgraph
        m = re.match(r"subgraph\s+(\w+)", line)
        if m:
            current_subgraph = m.group(1)
            continue

        if line == "end":
            current_subgraph = None
            continue

        # Nod in subgraph (ignora linii cu --> sau alte comenzi)
        if current_subgraph and line and "-->" not in line and not line.startswith("graph"):
            # Curata etichete: NodeName["Label"] -> NodeName
            node = re.match(r"(\w+)", line)
            if node:
                node_layers[node.group(1)] = current_subgraph

        # Muchie intre layere
        m = re.match(r"(\w+)\s*-->\s*(\w+)", line)
        if m:
            edges.add((m.group(1), m.group(2)))

    return node_layers, edges


# ── Metrici ───────────────────────────────────────────────────────────────────

@dataclass
class Metrics:
    node_precision:   float = 0.0
    node_recall:      float = 0.0
    node_f1:          float = 0.0
    layer_accuracy:   float = 0.0
    edge_precision:   float = 0.0
    edge_recall:      float = 0.0
    edge_f1:          float = 0.0
    jaccard_nodes:    float = 0.0
    jaccard_edges:    float = 0.0


def compute_metrics(pred_layers: dict, pred_edges: set,
                    gt_layers: dict,   gt_edges: set) -> Metrics:
    m = Metrics()

    pred_nodes = set(pred_layers.keys())
    gt_nodes   = set(gt_layers.keys())

    # Node precision / recall / F1
    tp_nodes = pred_nodes & gt_nodes
    m.node_precision = len(tp_nodes) / len(pred_nodes) if pred_nodes else 0.0
    m.node_recall    = len(tp_nodes) / len(gt_nodes)   if gt_nodes   else 0.0
    if m.node_precision + m.node_recall > 0:
        m.node_f1 = 2 * m.node_precision * m.node_recall / (m.node_precision + m.node_recall)

    # Layer accuracy — dintre nodurile comune, cate sunt in layer-ul corect
    common = tp_nodes
    if common:
        correct = sum(1 for n in common if pred_layers[n] == gt_layers[n])
        m.layer_accuracy = correct / len(common)

    # Edge precision / recall / F1
    tp_edges = pred_edges & gt_edges
    m.edge_precision = len(tp_edges) / len(pred_edges) if pred_edges else 0.0
    m.edge_recall    = len(tp_edges) / len(gt_edges)   if gt_edges   else 0.0
    if m.edge_precision + m.edge_recall > 0:
        m.edge_f1 = 2 * m.edge_precision * m.edge_recall / (m.edge_precision + m.edge_recall)

    # Jaccard
    union_nodes = pred_nodes | gt_nodes
    m.jaccard_nodes = len(tp_nodes) / len(union_nodes) if union_nodes else 0.0

    union_edges = pred_edges | gt_edges
    m.jaccard_edges = len(tp_edges) / len(union_edges) if union_edges else 0.0

    return m


# ── Incarcare date ────────────────────────────────────────────────────────────

def load_ground_truth(name: str) -> str | None:
    path = os.path.join(GROUND_TRUTH_DIR, f"{name}.json")
    if not os.path.exists(path):
        return None
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data["output"]["mermaid_diagram"]


def load_static(name: str) -> str | None:
    path = os.path.join(STATIC_DIR, f"{name}.mermaid")
    if not os.path.exists(path):
        return None
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def load_llm(llm_dir: str, name: str) -> str | None:
    for ext in [".mermaid", ".txt", ".md"]:
        path = os.path.join(llm_dir, f"{name}{ext}")
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                return f.read()
    return None


# ── Report ────────────────────────────────────────────────────────────────────

def fmt(v: float) -> str:
    return f"{v:.3f}"


def print_project_report(name: str, static_m: Metrics | None, llm_m: Metrics | None):
    print(f"\n{'─'*65}")
    print(f"  {name}")
    print(f"{'─'*65}")
    header = f"  {'Metric':<22} {'Static':>10}"
    if llm_m:
        header += f"  {'LLM':>10}"
    print(header)
    print(f"  {'-'*22} {'-'*10}" + (f"  {'-'*10}" if llm_m else ""))

    metrics = [
        ("Node Precision",  "node_precision"),
        ("Node Recall",     "node_recall"),
        ("Node F1",         "node_f1"),
        ("Layer Accuracy",  "layer_accuracy"),
        ("Edge Precision",  "edge_precision"),
        ("Edge Recall",     "edge_recall"),
        ("Edge F1",         "edge_f1"),
        ("Jaccard Nodes",   "jaccard_nodes"),
        ("Jaccard Edges",   "jaccard_edges"),
    ]

    for label, attr in metrics:
        sv = fmt(getattr(static_m, attr)) if static_m else "  N/A"
        row = f"  {label:<22} {sv:>10}"
        if llm_m:
            lv = fmt(getattr(llm_m, attr))
            row += f"  {lv:>10}"
        print(row)


def print_summary(all_static: list[Metrics], all_llm: list[Metrics]):
    attrs = [
        "node_precision", "node_recall", "node_f1",
        "layer_accuracy",
        "edge_precision", "edge_recall", "edge_f1",
        "jaccard_nodes", "jaccard_edges",
    ]
    labels = [
        "Node Precision", "Node Recall", "Node F1",
        "Layer Accuracy",
        "Edge Precision", "Edge Recall", "Edge F1",
        "Jaccard Nodes", "Jaccard Edges",
    ]

    print(f"\n{'='*65}")
    print(f"  SUMMARY ({len(all_static)} proiecte)")
    print(f"{'='*65}")
    header = f"  {'Metric':<22} {'Static AVG':>12}"
    if all_llm:
        header += f"  {'LLM AVG':>10}  {'Winner':>8}"
    print(header)
    print(f"  {'-'*22} {'-'*12}" + (f"  {'-'*10}  {'-'*8}" if all_llm else ""))

    for label, attr in zip(labels, attrs):
        s_avg = sum(getattr(m, attr) for m in all_static) / len(all_static)
        row = f"  {label:<22} {fmt(s_avg):>12}"
        if all_llm:
            l_avg = sum(getattr(m, attr) for m in all_llm) / len(all_llm)
            winner = "LLM ✓" if l_avg > s_avg else "Static ✓" if s_avg > l_avg else "Tie"
            row += f"  {fmt(l_avg):>10}  {winner:>8}"
        print(row)

    # Salveaza CSV
    rows = []
    rows.append("metric,static_avg" + (",llm_avg" if all_llm else ""))
    for label, attr in zip(labels, attrs):
        s_avg = sum(getattr(m, attr) for m in all_static) / len(all_static)
        row = f"{label},{s_avg:.4f}"
        if all_llm:
            l_avg = sum(getattr(m, attr) for m in all_llm) / len(all_llm)
            row += f",{l_avg:.4f}"
        rows.append(row)

    with open("evaluation_results.csv", "w") as f:
        f.write("\n".join(rows))
    print(f"\n  Rezultate salvate in: evaluation_results.csv")


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--llm-dir",  default=None, help="Folder cu outputurile LLM (.mermaid/.txt)")
    parser.add_argument("--project",  default=None, help="Evalueaza un singur proiect")
    parser.add_argument("--verbose",  action="store_true", help="Afiseaza detalii per proiect")
    args = parser.parse_args()

    # Selectie proiecte
    if args.project:
        projects = [args.project]
    else:
        projects = sorted(
            os.path.splitext(f)[0]
            for f in os.listdir(STATIC_DIR)
            if f.endswith(".mermaid")
        )

    all_static_metrics = []
    all_llm_metrics    = []

    for name in projects:
        gt_raw     = load_ground_truth(name)
        static_raw = load_static(name)

        if not gt_raw or not static_raw:
            continue

        gt_layers,     gt_edges     = parse_mermaid(gt_raw)
        static_layers, static_edges = parse_mermaid(static_raw)

        static_m = compute_metrics(static_layers, static_edges, gt_layers, gt_edges)
        all_static_metrics.append(static_m)

        llm_m = None
        if args.llm_dir:
            llm_raw = load_llm(args.llm_dir, name)
            if llm_raw:
                llm_layers, llm_edges = parse_mermaid(llm_raw)
                llm_m = compute_metrics(llm_layers, llm_edges, gt_layers, gt_edges)
                all_llm_metrics.append(llm_m)

        if args.verbose or args.project:
            print_project_report(name, static_m, llm_m)

    if not args.project:
        print_summary(all_static_metrics, all_llm_metrics if all_llm_metrics else None)
        print(f"\n  Proiecte evaluate: {len(all_static_metrics)}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""
Analizeaza dimensiunile fisierelor PUML si estimeaza token count.
Ajuta la alegerea max_seq_length pentru training.

Utilizare: python check_puml_sizes.py
"""

import os

PUML_DIR = "diagrame_puml"

# Estimare tokeni: ~4 chars/token (conservativ pentru cod)
CHARS_PER_TOKEN = 4

# Overhead: system_prompt (~300) + user wrapper (~50) + mermaid output (~200)
OVERHEAD_TOKENS = 550


def analyze():
    files = []
    for f in os.listdir(PUML_DIR):
        if not f.endswith(".puml"):
            continue
        path = os.path.join(PUML_DIR, f)
        chars = os.path.getsize(path)
        tokens = chars // CHARS_PER_TOKEN
        total_tokens = tokens + OVERHEAD_TOKENS
        files.append((f, chars, tokens, total_tokens))

    files.sort(key=lambda x: x[3], reverse=True)

    total = len(files)
    all_total = [x[3] for x in files]

    print(f"{'='*65}")
    print(f"  ANALIZA DIMENSIUNI PUML — {total} fisiere")
    print(f"{'='*65}")

    # Statistici generale
    print(f"\n📊 STATISTICI:")
    print(f"  Max tokens (cu overhead): {max(all_total):>7,}")
    print(f"  Avg tokens (cu overhead): {sum(all_total)//total:>7,}")
    print(f"  Min tokens (cu overhead): {min(all_total):>7,}")

    # Distributie pe praguri
    print(f"\n📏 DISTRIBUTIE PE max_seq_length:")
    for threshold in [2048, 4096, 8192, 16384, 32768]:
        over  = sum(1 for t in all_total if t > threshold)
        under = total - over
        pct   = under / total * 100
        print(f"  {threshold:>6} tokens: {under:>3}/{total} fit ({pct:.1f}%)  |  {over} depasesc")

    # Top 20 cele mai mari
    print(f"\n🔝 TOP 20 CELE MAI MARI:")
    print(f"  {'Fisier':<50} {'Chars':>8} {'Est.Tokens':>11} {'Total+OH':>10}")
    print(f"  {'-'*50} {'-'*8} {'-'*11} {'-'*10}")
    for fname, chars, tokens, total_tok in files[:20]:
        flag = " ⚠️" if total_tok > 4096 else ""
        print(f"  {fname:<50} {chars:>8,} {tokens:>11,} {total_tok:>10,}{flag}")

    # Recomandare
    print(f"\n💡 RECOMANDARE max_seq_length:")
    for threshold in [4096, 8192, 16384]:
        over = sum(1 for t in all_total if t > threshold)
        pct  = (total - over) / total * 100
        print(f"  {threshold:>6}: {pct:.1f}% fisiere fit ({over} excluse)")

    fit_4096 = sum(1 for t in all_total if t <= 4096)
    if fit_4096 / total < 0.7:
        print(f"\n  ⚠️  Doar {fit_4096/total*100:.0f}% fit in 4096.")
        print(f"      Recomandat: mareste max_seq_length la 8192 in notebook.")
    else:
        print(f"\n  ✅ {fit_4096/total*100:.0f}% fit in 4096 — OK pentru training.")


if __name__ == "__main__":
    analyze()

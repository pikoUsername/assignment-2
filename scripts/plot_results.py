import csv
from collections import defaultdict
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent.parent
TABLES, PLOTS = ROOT / "results/tables", ROOT / "results/plots"
FILES = ["workload1_random_access", "workload2_search", "workload3_insert_remove", "workload4_priority"]
TITLES = ["W1 random access", "W2 search", "W3 insert/remove", "W4 min-heap"]
COLORS = {"DynamicArray": "#2a78d6", "LinkedList": "#eb6834", "MinHeap": "#1baf7a"}


def load(name):
    rows = list(csv.DictReader(open(TABLES / f"{name}.csv")))
    for r in rows:
        r["n"], r["m"] = int(r["n"]), int(r["m"])
        r["ns_per_op"] = int(r["avg_time_ns"]) / r["m"]
        r["metric_per_op"] = int(r["metric_total"]) / r["m"]
    return rows


def plot(data, key, ylabel, filename):
    fig, axes = plt.subplots(2, 2, figsize=(11, 8))
    for ax, rows, title in zip(axes.flat, data, TITLES):
        lines = defaultdict(list)
        for r in rows:
            if not (r["operation"] == "peekMin" and key == "ns_per_op"):
                lines[(r["structure"], r["operation"])].append((r["n"], max(r[key], 0.01)))
        for (struct, op), pts in lines.items():
            style = "--" if op.startswith("remove") or op == "extractMin" else "-"
            marker = "s" if "middle" in op else "o"
            ax.plot(*zip(*sorted(pts)), style, marker=marker, color=COLORS[struct], label=f"{struct} {op}")
        ax.set(xscale="log", yscale="log", title=title, xlabel="n", ylabel=ylabel)
        ax.grid(alpha=0.3)
        ax.legend(fontsize=7)
    fig.tight_layout()
    fig.savefig(PLOTS / filename, dpi=130)


data = [load(f) for f in FILES]
PLOTS.mkdir(parents=True, exist_ok=True)
plot(data, "ns_per_op", "time per operation (ns)", "plot1_time_vs_n.png")
plot(data, "metric_per_op", "accesses / comparisons / moves per operation", "plot2_operations_vs_n.png")

with open(TABLES / "summary.md", "w") as out:
    for rows, title in zip(data, TITLES):
        out.write(f"### {title}\n\n| structure | operation | n | m | avg time (ms) | ns/op | metric | total | per op |\n")
        out.write("|---|---|---:|---:|---:|---:|---|---:|---:|\n")
        for r in rows:
            out.write(f"| {r['structure']} | {r['operation']} | {r['n']} | {r['m']} | {int(r['avg_time_ns']) / 1e6:.3f} "
                      f"| {r['ns_per_op']:.1f} | {r['metric']} | {r['metric_total']} | {r['metric_per_op']:.1f} |\n")
        out.write("\n")

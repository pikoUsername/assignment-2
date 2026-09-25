"""
Builds plots (results/plots/*.png) and markdown tables (results/tables/summary.md)
from the CSV files written by Benchmark.java.

Usage:  python scripts/plot_results.py
Needs:  matplotlib
"""
import csv
import math
from collections import defaultdict
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TABLES = ROOT / "results" / "tables"
PLOTS = ROOT / "results" / "plots"

# Categorical colours (fixed order): series identity follows the structure/operation.
COLORS = {
    "DynamicArray": "#2a78d6",
    "LinkedList": "#eb6834",
    "MinHeap": "#1baf7a",
}
OP_COLORS = ["#2a78d6", "#eb6834", "#1baf7a", "#eda100"]
INK = "#0b0b0b"
INK_2 = "#52514e"
GRID = "#e4e3df"

plt.rcParams.update({
    "figure.facecolor": "#fcfcfb",
    "axes.facecolor": "#fcfcfb",
    "axes.edgecolor": GRID,
    "axes.labelcolor": INK_2,
    "axes.titlecolor": INK,
    "axes.titlesize": 11,
    "axes.titleweight": "bold",
    "axes.grid": True,
    "grid.color": GRID,
    "grid.linewidth": 0.8,
    "xtick.color": INK_2,
    "ytick.color": INK_2,
    "legend.frameon": False,
    "legend.labelcolor": INK,
    "font.size": 9,
    "lines.linewidth": 2,
    "lines.markersize": 6,
})


def load(name):
    with open(TABLES / name, newline="") as f:
        rows = list(csv.DictReader(f))
    for r in rows:
        for key in ("n", "m", "metric_total"):
            r[key] = int(r[key])
        for key in ("avg_time_ns", "avg_time_ms", "time_per_op_ns", "metric_per_op"):
            r[key] = float(r[key])
    return rows


def series(rows, key_fn):
    out = defaultdict(list)
    for r in rows:
        out[key_fn(r)].append(r)
    for k in out:
        out[k].sort(key=lambda r: r["n"])
    return out


def style_loglog(ax, title, ylabel):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_title(title, loc="left")
    ax.set_xlabel("n (elements initially stored)")
    ax.set_ylabel(ylabel)
    ax.set_xticks([100, 1_000, 10_000, 100_000])
    ax.set_xticklabels(["100", "1k", "10k", "100k"])
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)


def plot_line(ax, xs, ys, label, color, marker="o", dashed=False):
    ax.plot(xs, ys, label=label, color=color, marker=marker,
            linestyle="--" if dashed else "-", markeredgecolor="#fcfcfb", markeredgewidth=1.5)


def main():
    PLOTS.mkdir(parents=True, exist_ok=True)
    w1 = load("workload1_random_access.csv")
    w2 = load("workload2_search.csv")
    w3 = load("workload3_insert_remove.csv")
    w4 = load("workload4_priority.csv")

    # ---------------------------------------------------------------- Plot 1: time vs n
    fig, axes = plt.subplots(2, 2, figsize=(11, 9.5))
    for ax, rows, title in ((axes[0][0], w1, "W1 Random access: 10,000 get(i)"),
                            (axes[0][1], w2, "W2 Search: 1,000 contains(x)")):
        for name, s in series(rows, lambda r: r["structure"]).items():
            plot_line(ax, [r["n"] for r in s], [r["avg_time_ms"] for r in s], name, COLORS[name])
        style_loglog(ax, title, "average time (ms)")
        ax.legend()

    ax = axes[1][0]
    markers = {"front": "o", "middle": "s"}
    for (struct, op), s in sorted(series(w3, lambda r: (r["structure"], r["operation"])).items()):
        kind, pos = op.split("_")
        plot_line(ax, [r["n"] for r in s], [r["time_per_op_ns"] for r in s],
                  f"{struct} {kind} {pos}", COLORS[struct], markers[pos], dashed=(kind == "remove"))
    style_loglog(ax, "W3 Insert / remove at index 0 and size/2", "time per operation (ns)")
    ax.legend(fontsize=7, ncol=2, loc="upper center", bbox_to_anchor=(0.5, -0.16))

    ax = axes[1][1]
    # peekMin is left out: it costs < 1 ns, below System.nanoTime() resolution (see table)
    for i, op in enumerate(("insert", "extractMin")):
        s = sorted((r for r in w4 if r["operation"] == op), key=lambda r: r["n"])
        plot_line(ax, [r["n"] for r in s], [r["time_per_op_ns"] for r in s], op, OP_COLORS[[0, 2][i]])
    style_loglog(ax, "W4 MinHeap: n insert, then n extractMin", "time per operation (ns)")
    ax.legend()
    fig.suptitle("Plot 1 - Execution time vs n (log-log)", x=0.01, ha="left", fontweight="bold", color=INK)
    fig.tight_layout()
    fig.savefig(PLOTS / "plot1_time_vs_n.png", dpi=150)
    plt.close(fig)

    # ---------------------------------------------------------------- Plot 2: operations vs n
    fig, axes = plt.subplots(2, 2, figsize=(11, 9.5))
    for ax, rows, title, ylabel in (
            (axes[0][0], w1, "W1 Element accesses for 10,000 get(i)", "element accesses"),
            (axes[0][1], w2, "W2 Comparisons for 1,000 contains(x)", "comparisons")):
        for name, s in series(rows, lambda r: r["structure"]).items():
            dashed = name == "LinkedList" and rows is w2   # both W2 lines coincide
            plot_line(ax, [r["n"] for r in s], [r["metric_total"] for r in s], name, COLORS[name],
                      dashed=dashed)
        style_loglog(ax, title, ylabel)
        ax.legend()

    ax = axes[1][0]
    for (struct, op), s in sorted(series(w3, lambda r: (r["structure"], r["operation"])).items()):
        kind, pos = op.split("_")
        plot_line(ax, [r["n"] for r in s], [r["metric_per_op"] for r in s],
                  f"{struct} {kind} {pos}", COLORS[struct], markers[pos], dashed=(kind == "remove"))
    style_loglog(ax, "W3 Accesses + moves per operation", "accesses + moves per op")
    ax.legend(fontsize=7, ncol=2, loc="upper center", bbox_to_anchor=(0.5, -0.16))

    ax = axes[1][1]
    for i, op in enumerate(("insert", "extractMin")):
        s = [r for r in w4 if r["operation"] == op]
        s.sort(key=lambda r: r["n"])
        plot_line(ax, [r["n"] for r in s], [r["metric_total"] for r in s], op, OP_COLORS[[0, 2][i]])
    ns = [100, 1_000, 10_000, 100_000]
    ax.plot(ns, [n * math.log2(n) for n in ns], color=INK_2, linewidth=1, linestyle=":",
            label="reference n log2 n")
    ax.plot(ns, ns, color=INK_2, linewidth=1, linestyle="-.", label="reference n")
    style_loglog(ax, "W4 Heap comparisons (n inserts, n extractions)", "comparisons")
    ax.legend()
    fig.suptitle("Plot 2 - Accesses / comparisons / moves vs n (log-log)", x=0.01, ha="left",
                 fontweight="bold", color=INK)
    fig.tight_layout()
    fig.savefig(PLOTS / "plot2_operations_vs_n.png", dpi=150)
    plt.close(fig)

    # ---------------------------------------------------------------- Plot 3: W1 per-get cost
    fig, ax = plt.subplots(figsize=(6.5, 4))
    for name, s in series(w1, lambda r: r["structure"]).items():
        plot_line(ax, [r["n"] for r in s], [r["time_per_op_ns"] for r in s], name, COLORS[name])
    style_loglog(ax, "Plot 3 - W1 time per get(i): flat vs linear", "ns per get")
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOTS / "plot3_get_per_op.png", dpi=150)
    plt.close(fig)

    # ---------------------------------------------------------------- Plot 4: heap normalised
    fig, ax = plt.subplots(figsize=(6.5, 4))
    for i, op in enumerate(("insert", "extractMin")):
        s = sorted((r for r in w4 if r["operation"] == op), key=lambda r: r["n"])
        plot_line(ax, [r["n"] for r in s], [r["metric_total"] / r["n"] for r in s],
                  op, OP_COLORS[[0, 2][i]])
    ax.plot(ns, [math.log2(n) for n in ns], color=INK_2, linewidth=1, linestyle=":", label="log2 n")
    ax.plot(ns, [2 * math.log2(n) for n in ns], color=INK_2, linewidth=1, linestyle="--", label="2 log2 n")
    ax.set_xscale("log")
    ax.set_xticks(ns)
    ax.set_xticklabels(["100", "1k", "10k", "100k"])
    ax.set_title("Plot 4 - Heap comparisons per operation", loc="left")
    ax.set_xlabel("n")
    ax.set_ylabel("comparisons per operation")
    for side in ("top", "right"):
        ax.spines[side].set_visible(False)
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOTS / "plot4_heap_comparisons_per_op.png", dpi=150)
    plt.close(fig)

    write_markdown(w1, w2, w3, w4)
    print("plots written to", PLOTS)


def fmt_ms(x):
    return f"{x:.3f}"


def write_markdown(w1, w2, w3, w4):
    lines = []

    def table(title, rows, metric_label):
        lines.append(f"### {title}\n")
        lines.append(f"| Structure | Operation | n | m | Avg time (ms) | ns / op | {metric_label} | per op | Theory |")
        lines.append("|---|---|---:|---:|---:|---:|---:|---:|---|")
        for r in rows:
            lines.append(
                f"| {r['structure']} | {r['operation']} | {r['n']:,} | {r['m']:,} | {fmt_ms(r['avg_time_ms'])} "
                f"| {r['time_per_op_ns']:.1f} | {r['metric_total']:,} | {r['metric_per_op']:.1f} | {r['theory']} |")
        lines.append("")

    by_struct = lambda rows: sorted(rows, key=lambda r: (r["structure"], r["n"]))
    table("Workload 1 - Random access", by_struct(w1), "Accesses")
    table("Workload 2 - Search", by_struct(w2), "Comparisons")
    order = ["insert_front", "remove_front", "insert_middle", "remove_middle"]
    table("Workload 3 - Insertion and removal", sorted(
        w3, key=lambda r: (order.index(r["operation"]), r["structure"], r["n"])), "Accesses + moves")
    table("Workload 4 - Priority processing", sorted(
        w4, key=lambda r: (["insert", "peekMin", "extractMin"].index(r["operation"]), r["n"])), "Comparisons")
    (TABLES / "summary.md").write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    main()

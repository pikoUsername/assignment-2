# Assignment 2: Algorithmic Analysis, Correctness and Performance Trade-offs

## Contents

1. [Overview](#1-overview)
2. [Complexity Analysis](#2-complexity-analysis)
3. [Correctness (loop invariants)](#3-correctness)
4. [Experimental Setup](#4-experimental-setup)
5. [Results](#5-results)
6. [Discussion](#6-discussion)
7. [Design Recommendations](#7-design-recommendations)
8. [Conclusion](#8-conclusion)

### Repository structure

```
assignment-2/
├── src/
│   ├── IndexedList.java     common interface of DynamicArray and LinkedList (+ counters)
│   ├── DynamicArray.java    resizable int array
│   ├── LinkedList.java      singly linked list with head and tail
│   ├── MinHeap.java         array-based binary min-heap
│   ├── Benchmark.java       the four workloads, writes CSV files
│   └── Tests.java           correctness tests (no external libraries)
├── scripts/
│   └── plot_results.py      builds plots and markdown tables from the CSV files
├── results/
│   ├── tables/              CSV output, summary.md, environment.txt
│   └── plots/               PNG plots
└── README.md
```

### How to build and run

Requires JDK 17+ (records and switch expressions are used) and Python 3 with matplotlib for the plots.

```bash
javac -d out src/*.java
java -cp out Tests                  # 26 test groups, exits with 1 if anything fails
java -Xmx2g -cp out Benchmark       # about 1 minute; writes results/tables/*.csv
python scripts/plot_results.py      # writes results/plots/*.png and results/tables/summary.md
```

---

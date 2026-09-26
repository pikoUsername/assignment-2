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

## 1. Overview

This assignment implements three data structures from scratch (for `int` values), analyses them, and
tests the analysis against measurements:

| Structure | Physical organisation | Operations |
|---|---|---|
| `DynamicArray` | one contiguous `int[]`, capacity doubles when it is full | `add(x)`, `add(i,x)`, `remove(i)`, `get(i)`, `contains(x)` |
| `LinkedList` | singly linked nodes, `head` and `tail` references | same five operations |
| `MinHeap` | complete binary tree stored in an `int[]` (children of `i` are `2i+1`, `2i+2`) | `insert(x)`, `peekMin()`, `extractMin()` |

Every structure counts its own basic operations so the benchmark can report more than time:

* **accesses**: element reads/writes (array slots, or list nodes visited during a traversal);
* **comparisons**: element comparisons (`contains`, heap sift-up/sift-down);
* **moves**: elements shifted or copied (array), links rewritten (list).

The aim is to compare the asymptotic predictions (O, Ω, Θ) with measured behaviour and to explain the gaps,
which come from constant factors, the memory hierarchy and the JIT compiler.

---

## 2. Complexity Analysis

Notation: `n` is the current number of elements and `i` is the index argument. **O** is an upper bound,
**Ω** is a lower bound, and **Θ** means both hold (a tight bound). The "average" column assumes
the index is uniformly random in its valid range and, for searches, that the value is present at a
uniformly random position or absent.

### 2.1 Complexity table

| Structure | Operation | Best | Average | Worst | Auxiliary space |
|---|---|---|---|---|---|
| Dynamic Array | `add(x)` (append) | Θ(1) | Θ(1) amortized | Θ(n) (resize) | Θ(1) amortized; Θ(n) temporary array when resizing |
| Dynamic Array | `add(i, x)` | Θ(1) (`i = n`) | Θ(n) | Θ(n) (`i = 0`) | Θ(1) (Θ(n) if a resize happens) |
| Dynamic Array | `remove(i)` | Θ(1) (`i = n-1`) | Θ(n) | Θ(n) (`i = 0`) | Θ(1) |
| Dynamic Array | `get(i)` | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| Dynamic Array | `contains(x)` | Θ(1) (first slot) | Θ(n) | Θ(n) (absent) | Θ(1) |
| Linked List | `add(x)` (append) | Θ(1) | Θ(1) | Θ(1) (uses `tail`) | Θ(1) (one node) |
| Linked List | `add(i, x)` | Θ(1) (`i = 0` or `i = n`) | Θ(n) | Θ(n) (`i = n-1`) | Θ(1) (one node) |
| Linked List | `remove(i)` | Θ(1) (`i = 0`) | Θ(n) | Θ(n) (`i = n-1`) | Θ(1) |
| Linked List | `get(i)` | Θ(1) (`i = 0`) | Θ(n) | Θ(n) (`i = n-1`) | Θ(1) |
| Linked List | `contains(x)` | Θ(1) (head) | Θ(n) | Θ(n) (absent) | Θ(1) |
| Min-Heap | `insert(x)` | Θ(1) (`x ≥ parent`) | Θ(1) expected for random keys, O(log n) in general | Θ(log n) (new minimum) | Θ(1) amortized (resize as above) |
| Min-Heap | `peekMin()` | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| Min-Heap | `extractMin()` | Θ(1) (e.g. all keys equal) | Θ(log n) | Θ(log n) | Θ(1) |

Storage: all three structures use Θ(n) space overall. The array wastes up to half of its capacity
right after a doubling. A list node costs about 24 bytes in HotSpot (12-byte header, 4-byte `int`,
4-byte compressed reference, and padding), which is about **6 times** the 4 bytes per element that
`int[]` needs.

### 2.2 Justification

**Dynamic Array**

* `get(i)`: the address is `base + 4·i`, so one memory access is enough whatever `i` and `n` are. Θ(1) in all cases.
* `add(i, x)`: the loop copies `data[n-1], …, data[i]` one slot to the right, which is exactly `n − i`
  moves. For uniformly random `i`, the expected number of moves is `n/2`, which is Θ(n). With `i = n`
  nothing moves (Θ(1)); with `i = 0` all `n` elements move (Θ(n)).
* `remove(i)`: copies `data[i+1 … n-1]` one slot to the left, `n − i − 1` moves, and has the same cases.
* `add(x)`: Θ(1) unless the array is full. A resize copies all `n` elements, but because capacity
  doubles, resizes happen at sizes 8, 16, 32, …, so the total copying over `N` appends is
  `8 + 16 + … + N < 2N`. That gives **amortized Θ(1)** per append, even though one particular append costs Θ(n).
* `contains(x)`: a linear scan. If `x` is at position `k`, it takes `k+1` comparisons (best case 1). An
  absent `x` takes exactly `n` comparisons. A present `x` at a uniformly random position takes `(n+1)/2`
  on average. All of these except the best case are Θ(n).

**Linked List**

* `get(i)`: the only way to reach node `i` is to follow `i` `next` references from `head`, so it visits `i + 1` nodes. The
  average over random `i` is `(n+1)/2`, which is Θ(n). This is the key difference from the array: **both operations are called
  "get", but one computes an address and the other walks a chain.**
* `add(0, x)` / `remove(0)`: only `head` is relinked, which is Θ(1) and independent of `n`.
* `add(i, x)` / `remove(i)` in general: the actual relinking is Θ(1), but finding the predecessor (node `i−1`)
  takes Θ(i). So the cost is **Θ(i)** and the average is Θ(n). The list saves *moves* but pays in
  *traversal*. `add(x)` at the end is Θ(1) because of the `tail` reference. In contrast, `remove(n−1)` is Θ(n):
  a singly linked list cannot step backwards from `tail` to its predecessor.
* `contains(x)`: the same linear scan and the same comparison counts as the array.

**Min-Heap**

A heap with `n` elements is a complete binary tree of height `h = ⌊log₂ n⌋`.

* `peekMin()`: the heap property means the root is the minimum, so it returns `heap[0]`. Θ(1).
* `insert(x)`: `x` is placed in the first free leaf and sifted up. Each iteration costs one comparison and moves one level up, so there are
  at most `h` iterations, which is O(log n). The worst case (a new minimum travels all the way to the root) is Θ(log n). The best case
  is one comparison, Θ(1). With random keys, about half of all nodes are leaves and a new key usually stops
  after a level or two, so the **expected** cost is a small constant number of levels. The experiment measures about 2.3 comparisons per insert.
* `extractMin()`: the last leaf moves to the root and is sifted down. There are up to 2 comparisons per level and `h` levels,
  so it is O(log n). The moved leaf is usually one of the largest keys, so it almost always sinks close to the bottom.
  That makes the average Θ(log n) as well: at most `2·log₂ n` comparisons, and the measured count approaches `1.7·log₂ n`.
* Heap sort follows as a corollary: `n` inserts plus `n` extractions cost Θ(n log n) in total. This is what Workload 4 does.

### 2.3 Operations that look similar but have different costs

* **`get(i)` on an array vs. a list**: Θ(1) vs. Θ(n). They share a name and a signature, but they are very different algorithms.
* **Insert at the front vs. the middle**: for the array, the front is the *worst* case (`n` shifts) and the middle is `n/2`.
  For the list, the front is the *best* case (Θ(1)) and the middle is `n/2` hops. The same call can be cheap on one
  structure and expensive on the other.
* **Array insert in the middle vs. list insert in the middle**: both are Θ(n) with the same number of elementary steps
  (about `n/2`). However, the array steps are sequential memory copies that the JIT vectorizes, while the list steps are dependent pointer loads.
  The measured time differs by about 12× (Section 5.3).
* **`add(x)` on the array**: it is "Θ(1)" only amortized. One individual call can take Θ(n) when it triggers a resize.
* **`peekMin` vs. `extractMin`**: reading the minimum is free, but removing it forces the heap to be repaired along a root-to-leaf path.

---

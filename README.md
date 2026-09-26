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

## 3. Correctness

### Proof 1: `DynamicArray.add(index, x)` (shifting loop)

```java
ensureCapacity(size + 1);
for (int i = size; i > index; i--) {
    data[i] = data[i - 1];
}
data[index] = x;
size++;
```

Let `s` be the size before the call, and let `A[0..s-1]` be the original contents. The precondition is
`0 ≤ index ≤ s`. `ensureCapacity` guarantees `data.length ≥ s + 1` and copies `A` unchanged into the (possibly new) array,
so every write below stays in bounds.

**Loop invariant.** At the start of each iteration (each time `i > index` is tested), with `index ≤ i ≤ s`:

1. `data[0 .. index-1] = A[0 .. index-1]` (the prefix is untouched);
2. `data[index .. i-1] = A[index .. i-1]` (the part not yet shifted is still in place);
3. `data[i+1 .. s] = A[i .. s-1]` (the part already shifted is one slot to the right).

**Initialization.** Before the first test, `i = s`. No element has been modified, so (1) and (2) hold because the
array equals `A`. Part (3) covers the range `data[s+1 .. s]`, which is empty, so it holds trivially.

**Maintenance.** Suppose the invariant holds and `i > index`. The body executes `data[i] = data[i-1]`.
Because `index ≤ i−1 ≤ i−1`, part (2) says `data[i-1] = A[i-1]`, so afterwards `data[i] = A[i-1]`.
Together with (3), this gives `data[i .. s] = A[i-1 .. s-1]`, which is part (3) for `i−1`. The body wrote only position `i`,
which is outside `0 .. i-2`, so part (2) for `i−1` (`data[index .. i-2] = A[index .. i-2]`) and part (1) still hold.
After `i--`, the invariant holds for the new value of `i`.

**Termination.** `i` starts at `s ≥ index` and decreases by exactly 1 per iteration, so the loop stops after
`s − index` iterations with `i = index`. At that point part (2) covers the empty range, and the invariant gives

* `data[0 .. index-1] = A[0 .. index-1]`,
* `data[index+1 .. s] = A[index .. s-1]`.

**Why this proves correctness.** The statement after the loop sets `data[index] = x` and `size = s + 1`. So
`data[0 .. s] = A[0], …, A[index-1], x, A[index], …, A[s-1]`, which is exactly the original sequence with
`x` inserted at position `index`. No element is lost or duplicated. The loop runs from the right end towards `index`, so
each value is read before it is overwritten. A left-to-right loop would overwrite `A[index+1]` before it had been copied.
The number of iterations, `s − index`, also gives the Θ(n − index) cost used in Section 2.

### Proof 2: `MinHeap.insert(x)` (sift-up loop)

```java
heap[size] = x; size++;
int i = size - 1;
while (i > 0) {
    int parent = (i - 1) / 2;
    if (heap[parent] <= heap[i]) break;
    swap(i, parent);
    i = parent;
}
```

Write `p(j) = (j−1)/2` for the parent of `j`. Before the call, `heap[0..s-1]` is a valid heap `H`, meaning
`heap[p(j)] ≤ heap[j]` for all `0 < j < s`. We call the pair `(p(j), j)` an *edge*.

**Loop invariant.** Each time the condition `i > 0` is tested:

* **(I1)** `heap[0 .. s]` is a permutation of the multiset `H ∪ {x}`;
* **(I2)** every edge satisfies the heap order **except possibly the edge `(p(i), i)`**;
* **(I3)** if `i > 0`, then for every child `c` of `i`: `heap[p(i)] ≤ heap[c]` (the grandparent is not larger than
  `i`'s children).

**Initialization.** `i = s` and `x` sits in a new leaf. (I1) holds trivially. All edges except `(p(s), s)` are edges of
`H`, which is a valid heap, so (I2) holds. Node `s` is the last element, so it has no children and (I3) holds vacuously.

**Maintenance.** Assume the invariant and `i > 0`, and let `q = p(i)`.

* If `heap[q] ≤ heap[i]`, the loop exits via `break` (see termination).
* Otherwise let `a = heap[q]` and `b = heap[i]`, with `b < a`. After the swap, `heap[q] = b` and `heap[i] = a`. Check every edge that
  touches `q` or `i`:
  * edge `(q, i)`: `b < a`, OK;
  * edge `(i, c)` for a child `c` of `i`: we need `a ≤ heap[c]`, which is exactly (I3) before the swap, OK;
  * edge `(q, t)` for the sibling `t` of `i`: (I2) gave `a ≤ heap[t]` and `b < a`, so `b ≤ heap[t]`, OK;
  * edge `(p(q), q)`: this may now be violated. It is the one exception that (I2) allows for the new `i = q`.

  For (I3) with the new `i = q` (if `q > 0`): the children of `q` are `i`, which now holds `a`, and `t`. Before the swap,
  the edge `(p(q), q)` was not the exceptional edge, so `heap[p(q)] ≤ a`, and `a ≤ heap[t]`. So `heap[p(q)]`
  is ≤ both children of `q`, OK. A swap does not change the multiset, so (I1) holds. Setting `i = q` re-establishes the invariant.

**Termination.** Each iteration replaces `i` by `p(i) = (i−1)/2 < i`, so the depth of `i` drops by one. The
loop therefore stops after at most `⌊log₂(s+1)⌋` iterations, which is the O(log n) bound. It can stop in one of two ways:

* `i = 0`: node 0 has no parent edge, so by (I2) **every** edge satisfies the heap order;
* `break` because `heap[p(i)] ≤ heap[i]`: the only edge that (I2) left unchecked is now known to be fine, so again every edge holds.

**Why this proves correctness.** In both cases the array `heap[0..s]` satisfies the heap property on every edge (I2 plus the
exit condition), and by (I1) it contains exactly the old elements plus `x`. So after `insert` the structure is a valid
min-heap of the right elements, and `peekMin()` = `heap[0]` is its minimum.

*(`extractMin` uses the symmetric sift-down invariant: "the heap order holds everywhere except possibly between `i`
and its children". The proof follows the same pattern. The tests call `isValidHeap()` after every single
insert and extract.)*

---

## 4. Experimental Setup

| Parameter | Value |
|---|---|
| **n** (elements initially stored) | 100, 1,000, 10,000, 100,000 |
| **m** (operations per workload) | W1: 10,000 `get`; W2: 1,000 `contains`; W3: 1,000 insertions and `min(1000, n)` removals per position; W4: `n` inserts + `n` extractMins (+10,000 `peekMin`) |
| Repetitions | **5 timed runs**, and the **average** is reported |
| Warm-up | 3 untimed global rounds of all workloads for n ≤ 10,000, plus 1 untimed run before every experiment (JIT compilation) |
| Timing | `System.nanoTime()` immediately around the operation loop only |
| Random seed | `new Random(42)`, re-created for every (workload, n), so both structures get **identical** data |
| Values | uniform random integers in `[0, 1,000,000)` |
| Environment | OpenJDK 25.0.3 (JetBrains Runtime), HotSpot 64-bit Server VM, Windows 11 x64, 16 logical cores, `-Xmx2g` (see `results/tables/environment.txt`) |

Input data (values, indices, search keys, values to insert) is generated **before** each timed section,
and building the structure is also untimed. Nothing is printed inside a timed section. The results
of the measured operations are summed into a `blackhole` variable, which is printed at the end, so the JIT cannot delete
the measured loops as dead code.

**Workloads**

* **W1 Random access**: build a structure with `n` random ints, generate 10,000 indices uniformly in `[0, n−1]`, then call
  `get(index)` for each. Metric: element accesses (array slots read, or list nodes visited).
* **W2 Search**: 1,000 queries. Even-numbered queries use a value picked from the stored data (a successful search).
  Odd-numbered queries use a value from `[1,000,000, 2,000,000)`, which is guaranteed absent (an unsuccessful search that scans the whole structure). Metric:
  element comparisons.
* **W3 Insertion/removal**: (A) 1,000 × `add(index, x)`, then (B) a rebuild of the original structure from the same data and
  `min(1000, n)` × `remove(index)`, with `index = 0` and, in part (C), the middle. Metric: accesses + moves (array: shifted/copied
  elements plus the slot written; list: nodes traversed plus links rewritten).
* **W4 Priority processing**: an empty `MinHeap`, `n` inserts (timed), 10,000 `peekMin` (timed separately), and `n` extractMins
  (timed). Metric: comparisons. After each run, the extracted sequence is checked to be non-decreasing **and** identical to
  `Arrays.sort` of the input.

**Design decisions to note**

* *Removals with n = 100.* A structure with 100 elements cannot support 1,000 removals, so for removals `m = min(1000, n)`, which
  is 100 for n = 100. The tables report the `ns / op` and `per op` columns, which normalise for this.
* *Middle index.* The workload uses `index = size()/2` of the **current** structure. A fixed `n/2` would become an invalid index during the
  removal phase whenever `m > n/2` (for example n = 1,000). Using the current middle keeps the operation "in the middle" throughout.
* *Counter overhead.* The counting (`accesses++` and so on) stays enabled during timing. It adds the same kind of
  constant overhead to both structures, so it does not change the comparison.

---

# Assignment 2: Data Structures Analysis

[Overview](#overview) · [Complexity](#complexity-analysis) · [Correctness](#correctness) · [Setup](#experimental-setup) · [Results](#results) · [Discussion](#discussion) · [Recommendations](#design-recommendations) · [Conclusion](#conclusion)

```
javac -d out src/*.java
java -cp out Tests
java -Xmx2g -cp out Benchmark
python scripts/plot_results.py
```

## Overview

I implemented a dynamic array, a singly linked list with head and tail pointers, and an array-based binary min-heap, all storing ints. Each structure counts its own element accesses, comparisons and moves, so the benchmark reports more than just time. The goal was to compare the theoretical complexity of each operation with what actually happens on a real machine.

## Complexity Analysis

**Dynamic array.** `get(i)` is Θ(1) in every case because the address is computed directly. `add(i, x)` and `remove(i)` shift the n − i elements after the position, so the best case is Θ(1) at the end and the average and worst cases are Θ(n). `add(x)` is amortized Θ(1): a single call is Θ(n) when the capacity doubles, but n appends copy fewer than 2n elements in total. `contains(x)` is Θ(1) in the best case and Θ(n) on average and in the worst case, and like all the other operations it uses O(1) extra space apart from the temporary array during a resize.

**Linked list.** Reaching position i means following i links from the head, so `get`, `add(i, x)` and `remove(i)` are Θ(1) at index 0 and Θ(n) on average and in the worst case. `add(x)` is always Θ(1) thanks to the tail pointer, while removing the last element is still Θ(n) because a singly linked list cannot step back to its predecessor. `contains` is the same Θ(n) linear scan as in the array. So the list avoids shifting elements but pays with traversal instead, and each node costs about six times more memory than an int in an array.

**Min-heap.** `peekMin` is Θ(1) because the minimum is always at the root. `insert` sifts the new leaf up, which gives Θ(1) in the best case and Θ(log n) in the worst case, and on random keys it is constant on average because a new key rarely climbs more than a level or two. `extractMin` moves the last leaf to the root and sifts it down with up to two comparisons per level, so it is Θ(log n) on average and in the worst case. All heap operations need O(1) extra space.

## Correctness

**Proof 1: `DynamicArray.add(index, x)`.** Let A be the original contents of size s. The invariant at every test of `i > index` is that `data[0..i-1]` still equals `A[0..i-1]` and `data[i+1..s]` equals `A[i..s-1]`. It holds initially because i = s and the shifted part is empty, and each iteration keeps it because `data[i] = data[i-1]` copies A[i-1] one slot right before i decreases. The loop terminates after s − index steps with i = index, so everything from index onwards has moved exactly one slot to the right. Writing x into `data[index]` then produces A with x inserted at the right position, without losing or duplicating any element.

**Proof 2: `MinHeap.insert(x)` (sift-up).** The invariant is that the array holds the old heap plus x, that every parent–child pair is ordered except possibly the pair (parent(i), i), and that parent(i) is no larger than i's children. Initially x is a new leaf and the old heap was valid, so this holds. If a swap happens, the larger parent value moves down to i, where it is still no larger than i's children by the third part of the invariant, and the smaller value moves up, where it is smaller than the sibling too, so only the pair above the new i can be wrong. Because i halves each time, the loop stops after at most log₂ n steps, either at the root or when the parent is no larger. In both cases every pair is ordered, so the result is a valid heap containing exactly the old elements plus x.

## Experimental Setup

Every workload is run for n = 100, 1,000, 10,000 and 100,000, with m = 10,000 gets in W1, 1,000 searches in W2 (half of the values are present and half are guaranteed absent), 1,000 insertions and min(1000, n) removals in W3, and n inserts plus n extractions in W4. Each experiment is repeated 5 times and I report the average of `System.nanoTime()` measured only around the operation loop. All data comes from `new Random(42)` and is generated before timing starts, so both lists get exactly the same input. A JIT warm-up pass runs first, because without it the first configurations were measured partly in the interpreter. The middle position in W3 is `size()/2` of the current structure, because a fixed n/2 would become an invalid index during the removals.

## Results

![time](results/plots/plot1_time_vs_n.png)
![operations](results/plots/plot2_operations_vs_n.png)

The raw numbers are in `results/tables/`, and `summary.md` there contains all the tables. In W1 the array did exactly one access per get at about 1.7 ns for every n, while the list visited about n/2 nodes and needed 45 µs per get at n = 100,000. In W2 both structures made the same 73.8 million comparisons at n = 100,000, which matches the expected 750n, but the array was about 3 times faster. In W3 the list stayed at a few nanoseconds per operation at the front while the array needed about 4 to 6 µs, but in the middle the array was 14 to 21 times faster even though both performed the same number of steps. In W4 insert needed a constant 2.3 comparisons, extractMin needed about 1.7·log₂ n comparisons, and the extracted order was verified every time.

## Discussion

The operation counts matched the theory almost exactly, and the time curves had the predicted slopes, flat for Θ(1) and linear for Θ(n). The results differed from the predictions at small n, where the timings are only a few microseconds and are dominated by noise and timer resolution, and in heap extraction, which slowed down faster than log n because the heap stopped fitting in cache. Two algorithms with the same Big-O can therefore run at very different speeds: shifting in the array is a sequential copy that the JIT vectorizes, while walking the list is a chain of dependent pointer loads, so the same n/2 steps cost 14 to 21 times more on the list. This is why the array wins for random access, scanning and most updates, why the list only makes sense when the work happens at the head, and why a heap, with O(1) peek and O(log n) updates, is the natural choice for priority processing. The right structure depends on which operations dominate the workload and where in the structure they happen.

## Design Recommendations

For random access, search and insertion or removal in the middle, I would use the dynamic array, since it is either asymptotically better or has much smaller constant factors. For a workload that mostly inserts and removes at the front, such as a stack or a queue, the linked list is the better choice because it never shifts elements. For repeatedly taking the smallest element, the min-heap is the right tool. When the workload is unknown, the dynamic array is the safest default.

## Conclusion

All three structures passed the tests, including comparisons against `ArrayList`, `java.util.LinkedList` and `PriorityQueue` on random operations. The measured counts confirmed the asymptotic analysis, and the timings showed that constant factors and memory layout decide which structure is faster in practice. Big-O tells you how the cost scales, but choosing a structure also requires measuring.

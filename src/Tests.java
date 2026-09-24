import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Self-contained test runner (no JUnit needed).
 * Run: java -cp out Tests
 *
 * Results are validated against java.util.ArrayList, java.util.LinkedList
 * and java.util.PriorityQueue.
 */
public class Tests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        List<Supplier<IndexedList>> lists = List.of(DynamicArray::new, LinkedList::new);
        for (Supplier<IndexedList> factory : lists) {
            String name = factory.get().name();
            run(name + ": empty structure", () -> testEmpty(factory.get()));
            run(name + ": one element", () -> testOneElement(factory.get()));
            run(name + ": multiple elements", () -> testMultiple(factory.get()));
            run(name + ": duplicate values", () -> testDuplicates(factory.get()));
            run(name + ": boundary indices", () -> testBoundaries(factory.get()));
            run(name + ": invalid indices", () -> testInvalidIndices(factory.get()));
            run(name + ": large input (100,000)", () -> testLarge(factory.get()));
            run(name + ": random ops vs ArrayList", () -> testRandomAgainstArrayList(factory));
            run(name + ": random ops vs java.util.LinkedList", () -> testRandomAgainstJavaLinkedList(factory));
        }

        run("MinHeap: empty structure", Tests::testHeapEmpty);
        run("MinHeap: one element", Tests::testHeapOne);
        run("MinHeap: multiple elements", Tests::testHeapMultiple);
        run("MinHeap: duplicate values", Tests::testHeapDuplicates);
        run("MinHeap: heap property after every insert/extract", Tests::testHeapPropertyEveryStep);
        run("MinHeap: large input, non-decreasing order", Tests::testHeapLarge);
        run("MinHeap: random ops vs PriorityQueue", Tests::testHeapAgainstPriorityQueue);
        run("MinHeap: sorted and reverse-sorted input", Tests::testHeapSortedInputs);

        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ lists

    private static void testEmpty(IndexedList list) {
        check(list.size() == 0, "size should be 0");
        check(list.isEmpty(), "should be empty");
        check(!list.contains(1), "empty list contains nothing");
        expectThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        expectThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    private static void testOneElement(IndexedList list) {
        list.add(7);
        check(list.size() == 1, "size 1");
        check(list.get(0) == 7, "get(0) == 7");
        check(list.contains(7), "contains 7");
        check(!list.contains(8), "does not contain 8");
        check(list.remove(0) == 7, "remove returns 7");
        check(list.isEmpty(), "empty after removal");
        list.add(0, 5);
        check(list.get(0) == 5, "add(0, x) into empty list");
        list.add(9);
        check(list.get(1) == 9, "append after add(0, x) uses correct tail");
    }

    private static void testMultiple(IndexedList list) {
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }
        list.add(0, -1);   // [-1,0..9]
        list.add(5, 100);  // [-1,0,1,2,3,100,4..9]
        list.add(list.size(), 50);
        assertContents(list, new int[]{-1, 0, 1, 2, 3, 100, 4, 5, 6, 7, 8, 9, 50});
        check(list.remove(5) == 100, "remove middle");
        check(list.remove(0) == -1, "remove first");
        check(list.remove(list.size() - 1) == 50, "remove last");
        assertContents(list, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        list.add(42);
        check(list.get(list.size() - 1) == 42, "append after removing last");
    }

    private static void testDuplicates(IndexedList list) {
        int[] values = {3, 3, 1, 3, 1};
        for (int v : values) {
            list.add(v);
        }
        assertContents(list, values);
        check(list.contains(3) && list.contains(1), "contains duplicates");
        list.remove(0);
        list.remove(0);
        assertContents(list, new int[]{1, 3, 1});
        check(list.contains(3), "still contains remaining 3");
    }

    private static void testBoundaries(IndexedList list) {
        for (int i = 0; i < 5; i++) {
            list.add(i * 10);
        }
        check(list.get(0) == 0, "first index");
        check(list.get(4) == 40, "last index");
        list.add(5, 50);           // index == size is allowed
        check(list.get(5) == 50, "insert at size");
        list.add(0, -10);
        check(list.get(0) == -10, "insert at 0");
        check(list.remove(list.size() - 1) == 50, "remove last index");
        check(list.remove(0) == -10, "remove index 0");
        assertContents(list, new int[]{0, 10, 20, 30, 40});
    }

    private static void testInvalidIndices(IndexedList list) {
        list.add(1);
        list.add(2);
        expectThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        expectThrows(IndexOutOfBoundsException.class, () -> list.get(2));
        expectThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        expectThrows(IndexOutOfBoundsException.class, () -> list.remove(2));
        expectThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 0));
        expectThrows(IndexOutOfBoundsException.class, () -> list.add(3, 0));
        assertContents(list, new int[]{1, 2});
    }

    private static void testLarge(IndexedList list) {
        int n = 100_000;
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        check(list.size() == n, "size n");
        check(list.get(0) == 0 && list.get(n / 2) == n / 2 && list.get(n - 1) == n - 1, "gets on large list");
        check(list.contains(n - 1), "contains last");
        check(!list.contains(n), "does not contain n");
        for (int i = 0; i < 1000; i++) {
            list.remove(0);
        }
        check(list.size() == n - 1000 && list.get(0) == 1000, "remove from front of large list");
    }

    private static void testRandomAgainstArrayList(Supplier<IndexedList> factory) {
        randomDifferential(factory.get(), new ArrayList<>(), 1);
    }

    private static void testRandomAgainstJavaLinkedList(Supplier<IndexedList> factory) {
        randomDifferential(factory.get(), new java.util.LinkedList<>(), 2);
    }

    /** Applies the same random sequence of operations to our list and a Java collection. */
    private static void randomDifferential(IndexedList mine, List<Integer> ref, long seed) {
        Random rnd = new Random(seed);
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(5);
            int value = rnd.nextInt(50);
            switch (op) {
                case 0 -> {
                    mine.add(value);
                    ref.add(value);
                }
                case 1 -> {
                    int idx = rnd.nextInt(ref.size() + 1);
                    mine.add(idx, value);
                    ref.add(idx, value);
                }
                case 2 -> {
                    if (!ref.isEmpty()) {
                        int idx = rnd.nextInt(ref.size());
                        check(mine.remove(idx) == ref.remove(idx), "remove mismatch at step " + step);
                    }
                }
                case 3 -> {
                    if (!ref.isEmpty()) {
                        int idx = rnd.nextInt(ref.size());
                        check(mine.get(idx) == ref.get(idx), "get mismatch at step " + step);
                    }
                }
                default -> check(mine.contains(value) == ref.contains(value), "contains mismatch at step " + step);
            }
            check(mine.size() == ref.size(), "size mismatch at step " + step);
        }
        int[] expected = ref.stream().mapToInt(Integer::intValue).toArray();
        assertContents(mine, expected);
    }

    private static void assertContents(IndexedList list, int[] expected) {
        check(list.size() == expected.length,
                "size " + list.size() + " != expected " + expected.length);
        for (int i = 0; i < expected.length; i++) {
            check(list.get(i) == expected[i],
                    "index " + i + ": " + list.get(i) + " != " + expected[i]);
        }
    }

    // ------------------------------------------------------------------ heap

    private static void testHeapEmpty() {
        MinHeap heap = new MinHeap();
        check(heap.isEmpty(), "new heap is empty");
        check(heap.isValidHeap(), "empty heap is valid");
        expectThrows(NoSuchElementException.class, heap::peekMin);
        expectThrows(NoSuchElementException.class, heap::extractMin);
    }

    private static void testHeapOne() {
        MinHeap heap = new MinHeap();
        heap.insert(5);
        check(heap.peekMin() == 5, "peekMin with one element");
        check(heap.size() == 1, "peekMin does not remove");
        check(heap.extractMin() == 5, "extractMin with one element");
        check(heap.isEmpty(), "empty after extract");
        expectThrows(NoSuchElementException.class, heap::extractMin);
    }

    private static void testHeapMultiple() {
        MinHeap heap = new MinHeap(1); // forces several resizes
        int[] values = {9, 4, 7, 1, 8, 2, 6, 3, 5, 0};
        for (int v : values) {
            heap.insert(v);
        }
        check(heap.peekMin() == 0, "min is 0");
        for (int expected = 0; expected < 10; expected++) {
            check(heap.extractMin() == expected, "extract " + expected);
        }
    }

    private static void testHeapDuplicates() {
        MinHeap heap = new MinHeap();
        int[] values = {3, 1, 3, 1, 2, 2, 1};
        for (int v : values) {
            heap.insert(v);
        }
        int[] sorted = values.clone();
        Arrays.sort(sorted);
        for (int expected : sorted) {
            check(heap.extractMin() == expected, "duplicate order");
            check(heap.isValidHeap(), "valid after extract with duplicates");
        }
    }

    private static void testHeapPropertyEveryStep() {
        MinHeap heap = new MinHeap();
        Random rnd = new Random(42);
        for (int i = 0; i < 2000; i++) {
            heap.insert(rnd.nextInt(1000));
            check(heap.isValidHeap(), "heap property after insert #" + i);
        }
        int prev = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int x = heap.extractMin();
            check(x >= prev, "non-decreasing extraction");
            check(heap.isValidHeap(), "heap property after extract");
            prev = x;
        }
    }

    private static void testHeapLarge() {
        int n = 100_000;
        MinHeap heap = new MinHeap();
        Random rnd = new Random(42);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt();
            heap.insert(values[i]);
        }
        check(heap.isValidHeap(), "large heap valid");
        Arrays.sort(values);
        for (int i = 0; i < n; i++) {
            check(heap.extractMin() == values[i], "large heap order at " + i);
        }
    }

    private static void testHeapAgainstPriorityQueue() {
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        Random rnd = new Random(7);
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(3);
            if (op == 0 || pq.isEmpty()) {
                int v = rnd.nextInt(100) - 50;
                heap.insert(v);
                pq.add(v);
            } else if (op == 1) {
                check(heap.peekMin() == pq.peek(), "peekMin mismatch at step " + step);
            } else {
                check(heap.extractMin() == pq.poll(), "extractMin mismatch at step " + step);
            }
            check(heap.size() == pq.size(), "size mismatch at step " + step);
        }
    }

    private static void testHeapSortedInputs() {
        MinHeap asc = new MinHeap();
        MinHeap desc = new MinHeap();
        for (int i = 0; i < 1000; i++) {
            asc.insert(i);
            desc.insert(1000 - i);
        }
        check(asc.isValidHeap() && desc.isValidHeap(), "valid for sorted inputs");
        check(asc.peekMin() == 0 && desc.peekMin() == 1, "min of sorted inputs");
    }

    // ------------------------------------------------------------------ helpers

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " -> " + t.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " but nothing was thrown");
    }
}

import java.util.*;
import java.util.function.Supplier;

public class Tests {
    static int passed, failed;

    public static void main(String[] args) {
        for (Supplier<IndexedList> f : List.<Supplier<IndexedList>>of(DynamicArray::new, LinkedList::new)) {
            String name = f.get().name();
            run(name + " empty", () -> {
                IndexedList l = f.get();
                check(l.size() == 0 && !l.contains(1));
                expectThrows(() -> l.get(0));
                expectThrows(() -> l.remove(0));
            });
            run(name + " one element", () -> {
                IndexedList l = f.get();
                l.add(7);
                check(l.get(0) == 7 && l.contains(7) && !l.contains(8));
                check(l.remove(0) == 7 && l.size() == 0);
                l.add(0, 5);
                l.add(9);
                check(l.get(0) == 5 && l.get(1) == 9);
            });
            run(name + " multiple, duplicates, boundaries", () -> {
                IndexedList l = f.get();
                for (int v : new int[]{3, 3, 1, 3, 1}) l.add(v);
                l.add(0, -1);
                l.add(l.size(), 50);
                l.add(3, 100);
                assertContents(l, -1, 3, 3, 100, 1, 3, 1, 50);
                check(l.remove(3) == 100 && l.remove(0) == -1 && l.remove(l.size() - 1) == 50);
                assertContents(l, 3, 3, 1, 3, 1);
            });
            run(name + " invalid indices", () -> {
                IndexedList l = f.get();
                l.add(1);
                l.add(2);
                expectThrows(() -> l.get(-1));
                expectThrows(() -> l.get(2));
                expectThrows(() -> l.remove(2));
                expectThrows(() -> l.add(-1, 0));
                expectThrows(() -> l.add(3, 0));
            });
            run(name + " large input", () -> {
                IndexedList l = f.get();
                for (int i = 0; i < 100_000; i++) l.add(i);
                check(l.get(50_000) == 50_000 && l.contains(99_999) && !l.contains(100_000));
                for (int i = 0; i < 1000; i++) l.remove(0);
                check(l.size() == 99_000 && l.get(0) == 1000);
            });
            run(name + " random ops vs ArrayList", () -> compareWithJava(f.get(), new ArrayList<>()));
            run(name + " random ops vs java.util.LinkedList", () -> compareWithJava(f.get(), new java.util.LinkedList<>()));
        }

        run("MinHeap empty and one element", () -> {
            MinHeap h = new MinHeap();
            expectThrows(h::peekMin);
            expectThrows(h::extractMin);
            h.insert(5);
            check(h.peekMin() == 5 && h.size() == 1 && h.extractMin() == 5 && h.isEmpty());
        });
        run("MinHeap duplicates, heap property after every operation, order", () -> {
            MinHeap h = new MinHeap();
            Random rnd = new Random(42);
            for (int i = 0; i < 2000; i++) {
                h.insert(rnd.nextInt(100));
                check(h.isValidHeap());
            }
            int prev = Integer.MIN_VALUE;
            while (!h.isEmpty()) {
                int x = h.extractMin();
                check(x >= prev && h.isValidHeap());
                prev = x;
            }
        });
        run("MinHeap large input vs sorting", () -> {
            MinHeap h = new MinHeap();
            int[] values = new Random(1).ints(100_000).toArray();
            for (int v : values) h.insert(v);
            Arrays.sort(values);
            for (int v : values) check(h.extractMin() == v);
        });
        run("MinHeap random ops vs PriorityQueue", () -> {
            MinHeap h = new MinHeap();
            PriorityQueue<Integer> pq = new PriorityQueue<>();
            Random rnd = new Random(7);
            for (int step = 0; step < 20_000; step++) {
                int op = rnd.nextInt(3);
                if (op == 0 || pq.isEmpty()) {
                    int v = rnd.nextInt(100);
                    h.insert(v);
                    pq.add(v);
                } else if (op == 1) check(h.peekMin() == pq.peek());
                else check(h.extractMin() == pq.poll());
            }
        });

        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void compareWithJava(IndexedList mine, List<Integer> ref) {
        Random rnd = new Random(3);
        for (int step = 0; step < 20_000; step++) {
            int op = rnd.nextInt(4), v = rnd.nextInt(50);
            if (op == 0) {
                int i = rnd.nextInt(ref.size() + 1);
                mine.add(i, v);
                ref.add(i, v);
            } else if (ref.isEmpty()) {
                mine.add(v);
                ref.add(v);
            } else if (op == 1) {
                int i = rnd.nextInt(ref.size());
                check(mine.remove(i) == ref.remove(i));
            } else if (op == 2) {
                int i = rnd.nextInt(ref.size());
                check(mine.get(i) == ref.get(i));
            } else check(mine.contains(v) == ref.contains(v));
            check(mine.size() == ref.size());
        }
    }

    static void assertContents(IndexedList l, int... expected) {
        check(l.size() == expected.length);
        for (int i = 0; i < expected.length; i++) check(l.get(i) == expected[i]);
    }

    static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("[FAIL] " + name + " " + t);
        }
    }

    static void check(boolean ok) {
        if (!ok) throw new AssertionError("check failed");
    }

    static void expectThrows(Runnable r) {
        try {
            r.run();
        } catch (IndexOutOfBoundsException | NoSuchElementException e) {
            return;
        }
        throw new AssertionError("expected an exception");
    }
}

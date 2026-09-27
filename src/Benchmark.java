import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class Benchmark {
    static final int[] NS = {100, 1_000, 10_000, 100_000};
    static final int RUNS = 5;
    static final long SEED = 42;
    static final List<Supplier<IndexedList>> LISTS = List.of(DynamicArray::new, LinkedList::new);
    static long blackhole;

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 3; i++) runAll(new int[]{100, 1_000, 10_000});

        Path dir = Path.of("results/tables");
        Files.createDirectories(dir);
        List<List<String>> results = runAll(NS);
        String[] files = {"workload1_random_access", "workload2_search", "workload3_insert_remove", "workload4_priority"};
        for (int w = 0; w < 4; w++) {
            try (PrintWriter out = new PrintWriter(dir.resolve(files[w] + ".csv").toFile())) {
                out.println("structure,operation,n,m,avg_time_ns,metric,metric_total");
                results.get(w).forEach(out::println);
            }
            results.get(w).forEach(System.out::println);
        }
        System.out.println("blackhole " + blackhole);
    }

    static List<List<String>> runAll(int[] sizes) {
        List<List<String>> r = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (int n : sizes) {
            randomAccess(n, r.get(0));
            search(n, r.get(1));
            insertRemove(n, r.get(2));
            priority(n, r.get(3));
        }
        return r;
    }

    static void randomAccess(int n, List<String> out) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        int[] indices = rnd.ints(10_000, 0, n).toArray();
        for (Supplier<IndexedList> f : LISTS) {
            IndexedList list = build(f, values);
            long total = 0;
            for (int run = 0; run <= RUNS; run++) {
                list.resetCounters();
                long t0 = System.nanoTime();
                for (int i : indices) blackhole += list.get(i);
                if (run > 0) total += System.nanoTime() - t0;
            }
            out.add(row(list.name(), "get", n, indices.length, total, "accesses", list.getAccesses()));
        }
    }

    static void search(int n, List<String> out) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        int[] queries = new int[1000];
        for (int i = 0; i < queries.length; i++)
            queries[i] = i % 2 == 0 ? values[rnd.nextInt(n)] : 1_000_000 + rnd.nextInt(1_000_000);
        for (Supplier<IndexedList> f : LISTS) {
            IndexedList list = build(f, values);
            long total = 0;
            for (int run = 0; run <= RUNS; run++) {
                list.resetCounters();
                long t0 = System.nanoTime();
                for (int q : queries) if (list.contains(q)) blackhole++;
                if (run > 0) total += System.nanoTime() - t0;
            }
            out.add(row(list.name(), "contains", n, queries.length, total, "comparisons", list.getComparisons()));
        }
    }

    static void insertRemove(int n, List<String> out) {
        Random rnd = new Random(SEED);
        int[] values = randomValues(rnd, n);
        int[] toInsert = randomValues(rnd, 1000);
        for (boolean middle : new boolean[]{false, true}) {
            String pos = middle ? "middle" : "front";
            for (Supplier<IndexedList> f : LISTS) {
                for (boolean insert : new boolean[]{true, false}) {
                    int m = insert ? toInsert.length : Math.min(1000, n);
                    long total = 0;
                    IndexedList list = null;
                    for (int run = 0; run <= RUNS; run++) {
                        list = build(f, values);
                        list.resetCounters();
                        long t0 = System.nanoTime();
                        for (int k = 0; k < m; k++) {
                            int index = middle ? list.size() / 2 : 0;
                            if (insert) list.add(index, toInsert[k]);
                            else blackhole += list.remove(index);
                        }
                        if (run > 0) total += System.nanoTime() - t0;
                    }
                    String op = (insert ? "insert_" : "remove_") + pos;
                    out.add(row(list.name(), op, n, m, total, "accesses+moves", list.getAccesses() + list.getMoves()));
                }
            }
        }
    }

    static void priority(int n, List<String> out) {
        int[] values = randomValues(new Random(SEED), n);
        long insertTime = 0, peekTime = 0, extractTime = 0, insertCmp = 0, extractCmp = 0;
        for (int run = 0; run <= RUNS; run++) {
            MinHeap heap = new MinHeap();
            long t0 = System.nanoTime();
            for (int v : values) heap.insert(v);
            long t1 = System.nanoTime();
            insertCmp = heap.getComparisons();
            for (int k = 0; k < 10_000; k++) blackhole += heap.peekMin();
            long t2 = System.nanoTime();
            int[] extracted = new int[n];
            for (int k = 0; k < n; k++) extracted[k] = heap.extractMin();
            long t3 = System.nanoTime();

            for (int k = 1; k < n; k++)
                if (extracted[k - 1] > extracted[k]) throw new IllegalStateException("order violated, n=" + n);
            extractCmp = heap.getComparisons() - insertCmp;
            if (run > 0) {
                insertTime += t1 - t0;
                peekTime += t2 - t1;
                extractTime += t3 - t2;
            }
        }
        out.add(row("MinHeap", "insert", n, n, insertTime, "comparisons", insertCmp));
        out.add(row("MinHeap", "peekMin", n, 10_000, peekTime, "comparisons", 0));
        out.add(row("MinHeap", "extractMin", n, n, extractTime, "comparisons", extractCmp));
    }

    static String row(String structure, String op, int n, int m, long totalNanos, String metric, long value) {
        return structure + "," + op + "," + n + "," + m + "," + totalNanos / RUNS + "," + metric + "," + value;
    }

    static int[] randomValues(Random rnd, int count) {
        return rnd.ints(count, 0, 1_000_000).toArray();
    }

    static IndexedList build(Supplier<IndexedList> f, int[] values) {
        IndexedList list = f.get();
        for (int v : values) list.add(v);
        return list;
    }
}

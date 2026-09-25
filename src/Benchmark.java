import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Runs the four workloads from the assignment and writes CSV files to results/tables/.
 *
 * Run: java -cp out Benchmark [outputDir]
 *
 * Rules followed:
 *  - n in {100, 1000, 10000, 100000}
 *  - every experiment is repeated RUNS = 5 times, the average is reported
 *  - timing uses System.nanoTime() around the operations only
 *  - all input data is generated with new Random(42) before the timed section
 *  - JIT warm-up: 3 global untimed rounds on n <= 10000, plus 1 untimed run before every experiment
 */
public class Benchmark {

    static final int[] NS = {100, 1_000, 10_000, 100_000};
    static final int RUNS = 5;
    static final long SEED = 42;
    static final int VALUE_RANGE = 1_000_000;

    static final int W1_OPS = 10_000;
    static final int W2_OPS = 1_000;
    static final int W3_OPS = 1_000;
    static final int PEEK_OPS = 10_000;

    static final int[] WARMUP_NS = {100, 1_000, 10_000};
    static final int WARMUP_ROUNDS = 3;

    /** Sizes used by the workload methods (NS for measurement, WARMUP_NS during warm-up). */
    static int[] sizes = NS;
    static boolean quiet = false;

    /** Results are fed into this so the JIT cannot remove the measured work. */
    static long blackhole = 0;

    static final List<Supplier<IndexedList>> LISTS = List.of(DynamicArray::new, LinkedList::new);

    record Row(String workload, String structure, String operation, int n, int m,
               double avgNanos, String metric, long metricValue, String theory) {

        String toCsv() {
            return String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%.0f,%.4f,%.2f,%s,%d,%.2f,%s",
                    workload, structure, operation, n, m, avgNanos, avgNanos / 1e6,
                    avgNanos / m, metric, metricValue, (double) metricValue / m, theory);
        }
    }

    static final String CSV_HEADER =
            "workload,structure,operation,n,m,avg_time_ns,avg_time_ms,time_per_op_ns,metric,metric_total,metric_per_op,theory";

    public static void main(String[] args) throws IOException {
        Path outDir = Paths.get(args.length > 0 ? args[0] : "results/tables");
        Files.createDirectories(outDir);

        System.out.printf("Java %s, %s %s, %d cores%n", System.getProperty("java.version"),
                System.getProperty("os.name"), System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors());

        // Global JIT warm-up: run every workload on the smaller sizes and throw the results away.
        // Without this the first configurations are measured partly in the interpreter.
        System.out.println("Warming up...");
        quiet = true;
        sizes = WARMUP_NS;
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            workload1RandomAccess();
            workload2Search();
            workload3InsertRemove();
            workload4PriorityProcessing();
        }
        quiet = false;
        sizes = NS;

        List<Row> w1 = workload1RandomAccess();
        write(outDir.resolve("workload1_random_access.csv"), w1);
        List<Row> w2 = workload2Search();
        write(outDir.resolve("workload2_search.csv"), w2);
        List<Row> w3 = workload3InsertRemove();
        write(outDir.resolve("workload3_insert_remove.csv"), w3);
        List<Row> w4 = workload4PriorityProcessing();
        write(outDir.resolve("workload4_priority.csv"), w4);

        List<Row> all = new ArrayList<>();
        all.addAll(w1);
        all.addAll(w2);
        all.addAll(w3);
        all.addAll(w4);
        write(outDir.resolve("all_results.csv"), all);

        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outDir.resolve("environment.txt")))) {
            pw.printf("java.version=%s%n", System.getProperty("java.version"));
            pw.printf("java.vm.name=%s%n", System.getProperty("java.vm.name"));
            pw.printf("os=%s %s %s%n", System.getProperty("os.name"),
                    System.getProperty("os.version"), System.getProperty("os.arch"));
            pw.printf("cores=%d%n", Runtime.getRuntime().availableProcessors());
            pw.printf("maxHeapMB=%d%n", Runtime.getRuntime().maxMemory() / (1024 * 1024));
            pw.printf("runs=%d, seed=%d, warmup=%d global rounds on n<=10000 + 1 untimed run per experiment%n",
                    RUNS, SEED, WARMUP_ROUNDS);
        }
        System.out.println("(blackhole " + blackhole + ")");
        System.out.println("CSV files written to " + outDir.toAbsolutePath());
    }

    // ------------------------------------------------------------------ Workload 1

    static List<Row> workload1RandomAccess() {
        log("\n=== Workload 1: random access, m = " + W1_OPS + " get(index) ===");
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            Random rnd = new Random(SEED);
            int[] values = randomValues(rnd, n);
            int[] indices = new int[W1_OPS];
            for (int i = 0; i < W1_OPS; i++) {
                indices[i] = rnd.nextInt(n);
            }
            for (Supplier<IndexedList> factory : LISTS) {
                IndexedList list = build(factory, values);
                long total = 0;
                for (int run = 0; run <= RUNS; run++) {   // run 0 = warm-up
                    list.resetCounters();
                    long sum = 0;
                    long t0 = System.nanoTime();
                    for (int idx : indices) {
                        sum += list.get(idx);
                    }
                    long t1 = System.nanoTime();
                    blackhole += sum;
                    if (run > 0) total += t1 - t0;
                }
                String theory = list instanceof DynamicArray ? "Theta(1) per get" : "Theta(n) per get";
                Row row = new Row("W1", list.name(), "get", n, W1_OPS, (double) total / RUNS,
                        "accesses", list.getAccesses(), theory);
                rows.add(row);
                print(row);
            }
        }
        return rows;
    }

    // ------------------------------------------------------------------ Workload 2

    /**
     * Search values: half are taken from the stored data (successful searches),
     * half are taken from [VALUE_RANGE, 2*VALUE_RANGE) and therefore never present (unsuccessful).
     */
    static List<Row> workload2Search() {
        log("\n=== Workload 2: search, m = " + W2_OPS + " contains(value) ===");
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            Random rnd = new Random(SEED);
            int[] values = randomValues(rnd, n);
            int[] queries = new int[W2_OPS];
            for (int i = 0; i < W2_OPS; i++) {
                queries[i] = (i % 2 == 0)
                        ? values[rnd.nextInt(n)]
                        : VALUE_RANGE + rnd.nextInt(VALUE_RANGE);
            }
            for (Supplier<IndexedList> factory : LISTS) {
                IndexedList list = build(factory, values);
                long total = 0;
                for (int run = 0; run <= RUNS; run++) {
                    list.resetCounters();
                    int found = 0;
                    long t0 = System.nanoTime();
                    for (int q : queries) {
                        if (list.contains(q)) found++;
                    }
                    long t1 = System.nanoTime();
                    blackhole += found;
                    if (run > 0) total += t1 - t0;
                }
                Row row = new Row("W2", list.name(), "contains", n, W2_OPS, (double) total / RUNS,
                        "comparisons", list.getComparisons(), "Theta(n) per search");
                rows.add(row);
                print(row);
            }
        }
        return rows;
    }

    // ------------------------------------------------------------------ Workload 3

    enum Position { FRONT, MIDDLE }

    /**
     * A: m insertions, B: m removals (structure rebuilt from the same data first).
     * FRONT uses index 0, MIDDLE uses index size/2 of the current structure.
     * For removals m = min(1000, n) because a structure with n = 100 only has 100 elements.
     */
    static List<Row> workload3InsertRemove() {
        log("\n=== Workload 3: insertion / removal at front and middle ===");
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            Random rnd = new Random(SEED);
            int[] values = randomValues(rnd, n);
            int[] toInsert = randomValues(rnd, W3_OPS);
            for (Position pos : Position.values()) {
                for (Supplier<IndexedList> factory : LISTS) {
                    rows.add(measureInsert(factory, values, toInsert, pos));
                    print(rows.get(rows.size() - 1));
                    rows.add(measureRemove(factory, values, pos));
                    print(rows.get(rows.size() - 1));
                }
            }
        }
        return rows;
    }

    static Row measureInsert(Supplier<IndexedList> factory, int[] values, int[] toInsert, Position pos) {
        int n = values.length;
        long total = 0;
        IndexedList list = null;
        for (int run = 0; run <= RUNS; run++) {
            list = build(factory, values);          // fresh copy of the original structure, untimed
            list.resetCounters();
            long t0 = System.nanoTime();
            for (int x : toInsert) {
                int index = pos == Position.FRONT ? 0 : list.size() / 2;
                list.add(index, x);
            }
            long t1 = System.nanoTime();
            blackhole += list.size();
            if (run > 0) total += t1 - t0;
        }
        boolean array = list instanceof DynamicArray;
        String metric = "accesses+moves";
        long value = list.getAccesses() + list.getMoves();
        String theory = theoryInsertRemove(array, pos);
        String op = "insert_" + pos.name().toLowerCase(Locale.ROOT);
        return new Row("W3", list.name(), op, n, toInsert.length, (double) total / RUNS, metric, value, theory);
    }

    static Row measureRemove(Supplier<IndexedList> factory, int[] values, Position pos) {
        int n = values.length;
        int m = Math.min(W3_OPS, n);
        long total = 0;
        IndexedList list = null;
        for (int run = 0; run <= RUNS; run++) {
            list = build(factory, values);          // "restore the original structure", untimed
            list.resetCounters();
            long sum = 0;
            long t0 = System.nanoTime();
            for (int k = 0; k < m; k++) {
                int index = pos == Position.FRONT ? 0 : list.size() / 2;
                sum += list.remove(index);
            }
            long t1 = System.nanoTime();
            blackhole += sum;
            if (run > 0) total += t1 - t0;
        }
        boolean array = list instanceof DynamicArray;
        String metric = "accesses+moves";
        long value = list.getAccesses() + list.getMoves();
        String theory = theoryInsertRemove(array, pos);
        String op = "remove_" + pos.name().toLowerCase(Locale.ROOT);
        return new Row("W3", list.name(), op, n, m, (double) total / RUNS, metric, value, theory);
    }

    static String theoryInsertRemove(boolean array, Position pos) {
        if (pos == Position.FRONT) {
            return array ? "Theta(n) shifts per op" : "Theta(1) per op";
        }
        return array ? "Theta(n) shifts per op" : "Theta(n) traversal per op";
    }

    // ------------------------------------------------------------------ Workload 4

    static List<Row> workload4PriorityProcessing() {
        log("\n=== Workload 4: priority processing (MinHeap) ===");
        List<Row> rows = new ArrayList<>();
        for (int n : sizes) {
            Random rnd = new Random(SEED);
            int[] values = randomValues(rnd, n);
            int[] expected = values.clone();
            Arrays.sort(expected);                   // reference order for verification

            long insertTotal = 0;
            long extractTotal = 0;
            long peekTotal = 0;
            long insertComparisons = 0;
            long extractComparisons = 0;
            for (int run = 0; run <= RUNS; run++) {
                MinHeap heap = new MinHeap();
                int[] out = new int[n];

                long t0 = System.nanoTime();
                for (int v : values) {
                    heap.insert(v);
                }
                long t1 = System.nanoTime();
                long cmpAfterInsert = heap.getComparisons();

                long peekSum = 0;
                long p0 = System.nanoTime();
                for (int k = 0; k < PEEK_OPS; k++) {
                    peekSum += heap.peekMin();
                }
                long p1 = System.nanoTime();

                long t2 = System.nanoTime();
                for (int k = 0; k < n; k++) {
                    out[k] = heap.extractMin();
                }
                long t3 = System.nanoTime();
                long cmpExtract = heap.getComparisons() - cmpAfterInsert;

                // verification is outside the timed sections
                for (int k = 1; k < n; k++) {
                    if (out[k - 1] > out[k]) {
                        throw new IllegalStateException("extractMin order violated at n=" + n + ", k=" + k);
                    }
                }
                if (!Arrays.equals(out, expected)) {
                    throw new IllegalStateException("extracted elements differ from sorted input at n=" + n);
                }
                blackhole += peekSum + out[n - 1];

                if (run > 0) {
                    insertTotal += t1 - t0;
                    peekTotal += p1 - p0;
                    extractTotal += t3 - t2;
                }
                insertComparisons = cmpAfterInsert;
                extractComparisons = cmpExtract;
            }
            Row ins = new Row("W4", "MinHeap", "insert", n, n, (double) insertTotal / RUNS,
                    "comparisons", insertComparisons, "O(log n) per op; O(n log n) total");
            Row peek = new Row("W4", "MinHeap", "peekMin", n, PEEK_OPS, (double) peekTotal / RUNS,
                    "comparisons", 0, "Theta(1) per op");
            Row ext = new Row("W4", "MinHeap", "extractMin", n, n, (double) extractTotal / RUNS,
                    "comparisons", extractComparisons, "O(log n) per op; Theta(n log n) total");
            rows.add(ins);
            rows.add(peek);
            rows.add(ext);
            print(ins);
            print(peek);
            print(ext);
            log("    n=" + n + ": extracted sequence verified non-decreasing");
        }
        return rows;
    }

    // ------------------------------------------------------------------ helpers

    static int[] randomValues(Random rnd, int count) {
        int[] a = new int[count];
        for (int i = 0; i < count; i++) {
            a[i] = rnd.nextInt(VALUE_RANGE);
        }
        return a;
    }

    static IndexedList build(Supplier<IndexedList> factory, int[] values) {
        IndexedList list = factory.get();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    static void log(String message) {
        if (!quiet) {
            System.out.println(message);
        }
    }

    static void print(Row r) {
        if (quiet) {
            return;
        }
        System.out.printf(Locale.ROOT, "%-3s %-13s %-14s n=%-7d m=%-6d avg=%12.3f ms  %-11s=%,d%n",
                r.workload(), r.structure(), r.operation(), r.n(), r.m(), r.avgNanos() / 1e6,
                r.metric(), r.metricValue());
    }

    static void write(Path file, List<Row> rows) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println(CSV_HEADER);
            for (Row r : rows) {
                pw.println(r.toCsv());
            }
        }
    }
}

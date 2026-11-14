import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Q1 - Building Text Under Load: String vs StringBuilder vs StringBuffer
 * (plus String.join / StringJoiner / String.format as the "alternative concept").
 *
 * Measures: average time (System.nanoTime), bytes allocated by this thread,
 * estimated intermediate String objects, and heap used before/after.
 * Raw data is written to q1_results.csv.
 */
public class StringBenchmark {

    static final int WARMUP = 2, RUNS = 5;
    static final com.sun.management.ThreadMXBean TMX =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static volatile int sink;                       // stops the JIT removing the work

    // ---------- the 3 core approaches + 3 alternatives ----------
    static String plus(String[] f) {                // what the current code does
        String s = "";
        for (String x : f) s += x + ";";
        return s;
    }
    static String builder(String[] f) {
        StringBuilder sb = new StringBuilder();
        for (String x : f) sb.append(x).append(';');
        return sb.toString();
    }
    static String buffer(String[] f) {
        StringBuffer sb = new StringBuffer();
        for (String x : f) sb.append(x).append(';');
        return sb.toString();
    }
    static String join(String[] f) { return String.join(";", f) + ";"; }
    static String joiner(String[] f) {
        StringJoiner j = new StringJoiner(";", "", ";");
        for (String x : f) j.add(x);
        return j.toString();
    }
    static String format(String[] f) {              // format used per field (typical usage)
        StringBuilder sb = new StringBuilder();
        for (String x : f) sb.append(String.format("%s;", x));
        return sb.toString();
    }

    interface Maker { String make(String[] f); }

    static String[] fields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = "FIELD" + (i % 1000) + "=VAL";   // ~13 chars
        return f;
    }

    /** one measured run: returns {nanos, allocatedBytes} for 'tickets' tickets of fields f */
    static long[] once(Maker m, String[] f, int tickets) {
        long a0 = TMX.getThreadAllocatedBytes(Thread.currentThread().getId());
        long t0 = System.nanoTime();
        int h = 0;
        for (int i = 0; i < tickets; i++) h += m.make(f).length();
        long t1 = System.nanoTime();
        long a1 = TMX.getThreadAllocatedBytes(Thread.currentThread().getId());
        sink = h;
        return new long[]{t1 - t0, a1 - a0};
    }

    static long usedHeap() {
        System.gc(); System.gc();
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    static PrintWriter csv;

    static void workload(String name, int fieldsPerTicket, int tickets) {
        String[] f = fields(fieldsPerTicket);
        Object[][] approaches = {
            {"String +", (Maker) StringBenchmark::plus, fieldsPerTicket},       // ~n intermediate Strings / ticket
            {"StringBuilder", (Maker) StringBenchmark::builder, 1},             // 1 final String / ticket
            {"StringBuffer", (Maker) StringBenchmark::buffer, 1},
            {"String.join", (Maker) StringBenchmark::join, 1},
            {"StringJoiner", (Maker) StringBenchmark::joiner, 1},
            {"String.format", (Maker) StringBenchmark::format, 2 * fieldsPerTicket},
        };
        System.out.printf("%n=== %s : %d fields x %d tickets ===%n", name, fieldsPerTicket, tickets);
        System.out.printf("%-14s %14s %16s %18s %14s%n",
                "Approach", "avg time (ms)", "allocated (MB)", "est. String objs", "heap delta(MB)");
        for (Object[] ap : approaches) {
            String label = (String) ap[0];
            Maker m = (Maker) ap[1];
            int perTicketObjs = (Integer) ap[2];
            for (int i = 0; i < WARMUP; i++) once(m, f, Math.max(1, tickets / 10));
            long before = usedHeap();
            double totalMs = 0, totalMb = 0;
            for (int i = 0; i < RUNS; i++) {
                long[] r = once(m, f, tickets);
                totalMs += r[0] / 1e6;
                totalMb += r[1] / 1048576.0;
                csv.printf("%s,%d,%d,%s,%d,%.4f,%.4f%n", name, fieldsPerTicket, tickets, label, i + 1, r[0] / 1e6, r[1] / 1048576.0);
            }
            long after = usedHeap();
            System.out.printf("%-14s %14.3f %16.2f %18d %14.2f%n", label, totalMs / RUNS, totalMb / RUNS,
                    (long) perTicketObjs * tickets, (after - before) / 1048576.0);
        }
    }

    // ---------- scaling of ONE ticket with n fields (the O(n^2) vs O(n) evidence) ----------
    static void scaling() {
        System.out.printf("%n=== SCALING: one ticket with n fields (average of %d runs) ===%n", RUNS);
        System.out.printf("%8s %14s %14s %16s %12s%n", "n", "String + (ms)", "Builder (ms)", "chars copied(+)", "ratio +/SB");
        int[] ns = {1000, 2000, 4000, 8000, 16000, 32000};
        for (int n : ns) {
            String[] f = fields(n);
            for (int i = 0; i < WARMUP; i++) { once(StringBenchmark::plus, f, 1); once(StringBenchmark::builder, f, 1); }
            double p = 0, b = 0;
            for (int i = 0; i < RUNS; i++) {
                p += once(StringBenchmark::plus, f, 1)[0] / 1e6;
                b += once(StringBenchmark::builder, f, 1)[0] / 1e6;
            }
            p /= RUNS; b /= RUNS;
            long len = (long) n * (f[0].length() + 1);
            long copied = len * n / 2;       // sum_{k=1..n} k*L  ~ n^2 L / 2
            System.out.printf("%8d %14.3f %14.4f %16d %12.1f%n", n, p, b, copied, p / b);
            csv.printf("SCALING,%d,1,String +,0,%.4f,0%n", n, p);
            csv.printf("SCALING,%d,1,StringBuilder,0,%.4f,0%n", n, b);
        }
    }

    // ---------- thread-safety demo: shared booking log ----------
    static void sharedLog() throws Exception {
        System.out.println("\n=== SHARED BOOKING LOG: 4 threads x 50,000 appends of 'LOG;' (expected length 800,000) ===");
        final int T = 4, N = 50_000, expected = T * N * 4;
        for (String kind : new String[]{"StringBuilder (unsync)", "StringBuffer", "StringBuilder+synchronized"}) {
            int bad = 0, exceptions = 0; long minLen = Long.MAX_VALUE;
            for (int trial = 0; trial < 5; trial++) {
                final StringBuilder sbd = new StringBuilder();
                final StringBuffer sbf = new StringBuffer();
                final Object lock = new Object();
                Thread[] th = new Thread[T];
                final int[] exc = {0};
                for (int t = 0; t < T; t++) {
                    th[t] = new Thread(() -> {
                        try {
                            for (int i = 0; i < N; i++) {
                                if (kind.startsWith("StringBuilder (")) sbd.append("LOG;");
                                else if (kind.equals("StringBuffer")) sbf.append("LOG;");
                                else synchronized (lock) { sbd.append("LOG;"); }
                            }
                        } catch (RuntimeException e) { synchronized (exc) { exc[0]++; } }
                    });
                    th[t].start();
                }
                for (Thread x : th) x.join();
                int len = kind.equals("StringBuffer") ? sbf.length() : sbd.length();
                if (len != expected) bad++;
                exceptions += exc[0];
                minLen = Math.min(minLen, len);
            }
            System.out.printf("%-28s corrupted trials: %d/5   threads crashed: %d   shortest length: %d%n",
                    kind, bad, exceptions, minLen);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + System.getProperty("java.version") + " | cores=" + Runtime.getRuntime().availableProcessors());
        csv = new PrintWriter(new FileWriter("q1_results.csv"));
        csv.println("workload,fields,tickets,approach,run,time_ms,allocated_MB");
        workload("W1 short ticket",         10, 100_000);   // contrasting workload 1
        workload("W2 many big tickets",     50,  10_000);   // contrasting workload 2 (given in the question)
        workload("W3 one huge ticket",   20_000,       1);  // my own workload: one very long text
        scaling();
        sharedLog();
        csv.close();
        System.out.println("\nRaw data written to q1_results.csv");
    }
}

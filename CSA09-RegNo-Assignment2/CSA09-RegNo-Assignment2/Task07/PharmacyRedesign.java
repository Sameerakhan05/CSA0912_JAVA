import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Q7 - Diagnose, Redesign and Validate  (based on Q4: Pharmacy inventory reports)
 *
 *  ORIGINAL  : HashMap<code,Medicine> + insertion list.  "Code-order report" copies all keys and RE-SORTS them every time.
 *  TREEMAP   : TreeMap reference baseline (the "just replace the collection" answer - NOT my redesign, used for honest comparison).
 *  REDESIGN  : HashMap + insertion list + a cached sorted array of Medicine references.
 *              New medicines are collected in a small 'pending' buffer; at report time only the buffer is sorted
 *              (k log k) and MERGED into the already-sorted array (n + k). Stock updates never touch the sorted array
 *              because it stores references to mutable Medicine objects.
 *
 * Workload (identical for all three): R cycles of { add k new medicines, U stock updates, L lookups, code report, insertion report }.
 * Raw data -> q7_results.csv
 */
public class PharmacyRedesign {

    static final class Med {
        final String code; int stock;
        Med(String c, int s) { code = c; stock = s; }
    }

    interface Inventory {
        void add(String code);
        Med get(String code);
        long reportByCode();            // returns an order-sensitive checksum so results can be compared
        long reportByInsertion();
    }

    static long fold(long h, Med m) { return h * 31 + m.code.hashCode() + m.stock; }

    // ---------- ORIGINAL ----------
    static class Original implements Inventory {
        final HashMap<String, Med> map = new HashMap<>();
        final ArrayList<Med> insertion = new ArrayList<>();          // same insertion index in all 3 designs: only the code report differs
        public void add(String c) { Med m = new Med(c, 100); if (map.put(c, m) == null) insertion.add(m); }
        public Med get(String c) { return map.get(c); }
        public long reportByCode() {
            ArrayList<String> keys = new ArrayList<>(map.keySet());       // O(n) copy
            if (COUNTING) keys.sort(COUNTER); else Collections.sort(keys); // O(n log n) EVERY report
            long h = 0; for (String k : keys) h = fold(h, map.get(k));     // n hash look-ups
            return h;
        }
        public long reportByInsertion() { long h = 0; for (Med m : insertion) h = fold(h, m); return h; }
    }

    // ---------- TreeMap baseline ----------
    static class TreeInv implements Inventory {
        final TreeMap<String, Med> map = new TreeMap<>();
        final ArrayList<Med> insertion = new ArrayList<>();
        public void add(String c) { Med m = new Med(c, 100); if (map.put(c, m) == null) insertion.add(m); }
        public Med get(String c) { return map.get(c); }
        public long reportByCode() { long h = 0; for (Med m : map.values()) h = fold(h, m); return h; }
        public long reportByInsertion() { long h = 0; for (Med m : insertion) h = fold(h, m); return h; }
    }

    // ---------- REDESIGN ----------
    static class Redesign implements Inventory {
        final HashMap<String, Med> map = new HashMap<>();
        final ArrayList<Med> insertion = new ArrayList<>();         // references, so the insertion report needs no look-ups
        Med[] sorted = new Med[0];                                  // cached, always sorted by code
        final ArrayList<Med> pending = new ArrayList<>();           // new since last report
        public void add(String c) {
            Med m = new Med(c, 100);
            if (map.put(c, m) == null) { insertion.add(m); pending.add(m); }
        }
        public Med get(String c) { return map.get(c); }
        public long reportByCode() {
            if (!pending.isEmpty()) merge();                        // O(k log k + n + k), only when something changed
            long h = 0; for (Med m : sorted) h = fold(h, m);        // no look-ups, no sorting
            return h;
        }
        private void merge() {
            Med[] add = pending.toArray(new Med[0]);
            if (COUNTING) Arrays.sort(add, (a, b) -> COUNTER.compare(a.code, b.code)); else Arrays.sort(add, BY_CODE);
            Med[] out = new Med[sorted.length + add.length];
            int i = 0, j = 0, o = 0;
            while (i < sorted.length && j < add.length) {
                int c = COUNTING ? COUNTER.compare(sorted[i].code, add[j].code) : sorted[i].code.compareTo(add[j].code);
                out[o++] = c <= 0 ? sorted[i++] : add[j++];
            }
            while (i < sorted.length) out[o++] = sorted[i++];
            while (j < add.length) out[o++] = add[j++];
            sorted = out; pending.clear();
        }
        public long reportByInsertion() { long h = 0; for (Med m : insertion) h = fold(h, m); return h; }
    }

    static final Comparator<Med> BY_CODE = Comparator.comparing(m -> m.code);
    static boolean COUNTING = false;
    static long compares = 0;
    static final Comparator<String> COUNTER = (a, b) -> { compares++; return a.compareTo(b); };

    // ---------- workload ----------
    /** initial codes use EVEN ids; new medicines use ODD ids so they interleave with existing codes */
    static String code(long id) { return String.format("MED%08d", id); }

    static List<String> shuffledInitial(int n) {
        List<String> l = new ArrayList<>(n);
        for (int i = 0; i < n; i++) l.add(code(2L * i));
        Collections.shuffle(l, new Random(5));
        return l;
    }

    /** runs the workload on 'inv'; returns checksum of all reports */
    static long run(Inventory inv, List<String> initial, int cycles, int k, int updates, int lookups, long seed) {
        for (String c : initial) inv.add(c);
        int n = initial.size();
        Random r = new Random(seed);
        long total = 0, nextNew = 1;
        for (int cycle = 0; cycle < cycles; cycle++) {
            for (int i = 0; i < k; i++) { inv.add(code(nextNew)); nextNew += 2 + 2 * r.nextInt(3); }   // new odd ids
            for (int i = 0; i < updates; i++) inv.get(initial.get(r.nextInt(n))).stock += 1;
            long h = 0;
            for (int i = 0; i < lookups; i++) h += inv.get(initial.get(r.nextInt(n))).stock;
            total += h + inv.reportByCode() * 7 + inv.reportByInsertion();
        }
        return total;
    }

    static final com.sun.management.ThreadMXBean TMX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    static PrintWriter csv;
    static volatile long sink;

    /** returns {avg ms, allocated MB}; 1 warm-up + 3 timed runs */
    static double[] measure(Class<? extends Inventory> cls, List<String> init, int cycles, int k, int updates, int lookups) throws Exception {
        sink = run(cls.getDeclaredConstructor().newInstance(), init, cycles, k, updates, lookups, 1);
        double ms = 0, mb = 0;
        for (int i = 0; i < 3; i++) {
            Inventory inv = cls.getDeclaredConstructor().newInstance();
            System.gc();
            long a0 = TMX.getThreadAllocatedBytes(Thread.currentThread().getId());
            long t0 = System.nanoTime();
            sink = run(inv, init, cycles, k, updates, lookups, 1);
            ms += (System.nanoTime() - t0) / 1e6;
            mb += (TMX.getThreadAllocatedBytes(Thread.currentThread().getId()) - a0) / 1048576.0;
        }
        return new double[]{ms / 3, mb / 3};
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + System.getProperty("java.version"));
        csv = new PrintWriter(new FileWriter("q7_results.csv"));
        csv.println("n,k_new_per_cycle,lookups_per_cycle,design,total_ms,allocated_MB");
        final int R = 100, U = 1000;

        // ---------- A. DIAGNOSIS evidence: where does the original spend its time? ----------
        System.out.println("\n=== A. DIAGNOSIS (Original, n=50,000, 100 cycles, k=20 new/cycle, 200 lookups/cycle) ===");
        {
            List<String> init = shuffledInitial(50_000);
            Original o = new Original();
            for (String c : init) o.add(c);
            long t0 = System.nanoTime(); for (int i = 0; i < 100; i++) sink = o.reportByInsertion(); double insMs = (System.nanoTime() - t0) / 1e6 / 100;
            // time sort part and rest-of-report part separately
            double sortMs = 0, rest = 0;
            for (int i = 0; i < 25; i++) {
                long a = System.nanoTime(); ArrayList<String> keys = new ArrayList<>(o.map.keySet()); long b = System.nanoTime();
                Collections.sort(keys); long c = System.nanoTime();
                long h = 0; for (String key : keys) h = fold(h, o.map.get(key)); long d = System.nanoTime(); sink = h;
                if (i >= 5) { sortMs += (c - b) / 1e6; rest += ((b - a) + (d - c)) / 1e6; }
            }
            sortMs /= 20; rest /= 20;
            COUNTING = true; compares = 0; o.reportByCode(); long cmpOrig = compares; COUNTING = false;
            System.out.printf("   one code-order report: sort = %.2f ms, copy keys + 50,000 look-ups = %.2f ms, (insertion-order report = %.2f ms)%n", sortMs, rest, insMs);
            System.out.printf("   sorting share of the code report = %.0f%%; string comparisons per report = %,d (n*log2(n) = %,.0f)%n",
                    100 * sortMs / (sortMs + rest), cmpOrig, 50_000 * Math.log(50_000) / Math.log(2));
            System.out.println("   Between two reports only k=20 keys changed (0.04% of the key set) - the other 49,980 are re-sorted needlessly.");
        }

        // ---------- B. operation counts (the redesign's measurable objective) ----------
        System.out.println("\n=== B. STRING COMPARISONS PER CODE REPORT (n=50,000 existing, k new since last report) ===");
        System.out.printf("%8s %18s %18s %10s%n", "k", "Original", "Redesign", "ratio");
        for (int k : new int[]{20, 500, 5_000, 50_000}) {
            List<String> init = shuffledInitial(50_000);
            Original o = new Original(); Redesign d = new Redesign();
            for (String c : init) { o.add(c); d.add(c); }
            o.reportByCode(); d.reportByCode();                         // prime: redesign now holds a sorted array
            for (int i = 0; i < k; i++) { String c = code(1 + 2L * i * (50_000 / Math.max(1, k))); o.add(c); d.add(c); }
            COUNTING = true;
            compares = 0; long ho = o.reportByCode(); long co = compares;
            compares = 0; long hd = d.reportByCode(); long cd = compares;
            COUNTING = false;
            System.out.printf("%8d %,18d %,18d %9.1fx   same output: %s%n", k, co, cd, (double) co / cd, ho == hd);
            csv.printf("50000,%d,0,comparisons_original,%d,0%n50000,%d,0,comparisons_redesign,%d,0%n", k, co, k, cd);
        }

        // ---------- C. whole-workload timing: Original vs TreeMap vs Redesign ----------
        System.out.println("\n=== C. WHOLE WORKLOAD (100 cycles): total time, average of 3 runs, plus MB allocated ===");
        System.out.printf("%8s %6s %9s | %-24s | %-24s | %-24s | %9s%n", "n", "k", "lookups", "Original ms (MB)", "TreeMap ms (MB)", "Redesign ms (MB)", "speed-up");
        int[][] cfg = {
            {20_000, 20, 200}, {50_000, 20, 200}, {100_000, 20, 200},          // realistic: few lookups per cycle
            {50_000, 20, 100_000},                                             // lookup-heavy
            {50_000, 500, 200}, {50_000, 5_000, 200}                        // sweep k: how many new medicines per report (k=50,000 is covered in table B)
        };
        for (int[] c : cfg) {
            List<String> init = shuffledInitial(c[0]);
            double[] o = measure(Original.class, init, R, c[1], U, c[2]);
            double[] t = measure(TreeInv.class, init, R, c[1], U, c[2]);
            double[] d = measure(Redesign.class, init, R, c[1], U, c[2]);
            // correctness: all three must give the same checksum
            long x1 = run(new Original(), init, 10, c[1], U, Math.min(c[2], 1000), 1);
            long x2 = run(new TreeInv(), init, 10, c[1], U, Math.min(c[2], 1000), 1);
            long x3 = run(new Redesign(), init, 10, c[1], U, Math.min(c[2], 1000), 1);
            boolean same = x1 == x2 && x2 == x3;
            System.out.printf("%8d %6d %9d | %10.1f (%8.0f)   | %10.1f (%8.0f)   | %10.1f (%8.0f)   | %8.1fx %s%n",
                    c[0], c[1], c[2], o[0], o[1], t[0], t[1], d[0], d[1], o[0] / d[0], same ? "" : "  OUTPUT MISMATCH!");
            csv.printf("%d,%d,%d,Original,%.3f,%.2f%n", c[0], c[1], c[2], o[0], o[1]);
            csv.printf("%d,%d,%d,TreeMap,%.3f,%.2f%n", c[0], c[1], c[2], t[0], t[1]);
            csv.printf("%d,%d,%d,Redesign,%.3f,%.2f%n", c[0], c[1], c[2], d[0], d[1]);
            csv.flush();
        }
        csv.close();
        System.out.println("\nRaw data written to q7_results.csv");
    }
}

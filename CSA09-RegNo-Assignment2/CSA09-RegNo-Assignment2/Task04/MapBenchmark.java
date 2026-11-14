import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Q4 - Choosing a Map for a Pharmacy Inventory.
 * Compares HashMap, TreeMap, LinkedHashMap (and Hashtable) + sorted array with Arrays.binarySearch.
 * Needs: lookup by code, report in CODE order, report in INSERTION order.
 * Raw data -> q4_results.csv
 */
public class MapBenchmark {

    record Medicine(String code, String name, int stock) {}

    static final int RUNS = 5, LOOKUPS = 100_000;
    static volatile long sink;
    static PrintWriter csv;

    static List<String> randomCodes(int n, long seed) {         // insertion order = random (NOT sorted)
        List<Integer> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) ids.add(i);
        Collections.shuffle(ids, new Random(seed));
        List<String> codes = new ArrayList<>(n);
        for (int id : ids) codes.add(String.format("MED%07d", id));
        return codes;
    }

    static double avg(double[] a) { double s = 0; for (double x : a) s += x; return s / a.length; }

    /** run body RUNS times (after 2 warm-ups) and return avg ms */
    static double timeIt(Supplier<Runnable> fresh) {
        for (int w = 0; w < 2; w++) fresh.get().run();
        double[] t = new double[RUNS];
        for (int r = 0; r < RUNS; r++) {
            Runnable job = fresh.get();
            long t0 = System.nanoTime();
            job.run();
            t[r] = (System.nanoTime() - t0) / 1e6;
        }
        return avg(t);
    }

    static Map<String, Medicine> build(Map<String, Medicine> m, List<String> codes) {
        for (String c : codes) m.put(c, new Medicine(c, "Drug-" + c, 100));
        return m;
    }

    static long usedHeap() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + System.getProperty("java.version"));
        csv = new PrintWriter(new FileWriter("q4_results.csv"));
        csv.println("size,structure,metric,value");
        Random rnd = new Random(7);

        for (int n : new int[]{10_000, 100_000, 500_000}) {
            List<String> codes = randomCodes(n, 99);                  // insertion order
            List<String> sortedCodes = new ArrayList<>(codes); Collections.sort(sortedCodes);
            String[] probes = new String[LOOKUPS];
            for (int i = 0; i < LOOKUPS; i++) probes[i] = codes.get(rnd.nextInt(n));

            System.out.printf("%n================ n = %,d medicines ================%n", n);
            System.out.printf("%-14s %10s %14s %16s %16s %10s %10s%n", "Structure", "build(ms)", "100k lookups", "ns / lookup", "code report(ms)", "code ok?", "ins. ok?");

            String[] names = {"HashMap", "TreeMap", "LinkedHashMap", "Hashtable"};
            Supplier<Map<String, Medicine>>[] makers = new Supplier[]{HashMap::new, TreeMap::new, LinkedHashMap::new, Hashtable::new};

            for (int k = 0; k < names.length; k++) {
                final Supplier<Map<String, Medicine>> mk = makers[k];
                final String name = names[k];

                double buildMs = timeIt(() -> () -> sink = build(mk.get(), codes).size());

                final Map<String, Medicine> map = build(mk.get(), codes);
                final ArrayList<String> insertionIndex = new ArrayList<>(codes);     // extra structure needed when the map cannot give insertion order

                double lookupMs = timeIt(() -> () -> { long h = 0; for (String p : probes) h += map.get(p).stock(); sink = h; });

                // report 1: all medicines in CODE order
                final List<String>[] codeOut = new List[1];
                double codeReportMs = timeIt(() -> () -> {
                    List<String> out = new ArrayList<>(map.size());
                    if (map instanceof TreeMap) { for (Medicine m : map.values()) out.add(m.code()); }          // natural order
                    else { List<String> keys = new ArrayList<>(map.keySet()); Collections.sort(keys);           // EXTRA WORK: copy + sort
                           for (String key : keys) out.add(map.get(key).code()); }
                    codeOut[0] = out; sink = out.size();
                });

                // report 2: all medicines in INSERTION order
                final List<String>[] insOut = new List[1];
                double insReportMs = timeIt(() -> () -> {
                    List<String> out = new ArrayList<>(map.size());
                    if (map instanceof LinkedHashMap) { for (Medicine m : map.values()) out.add(m.code()); }    // natural order
                    else { for (String key : insertionIndex) out.add(map.get(key).code()); }                    // EXTRA: parallel list + n lookups
                    insOut[0] = out; sink = out.size();
                });

                boolean codeOk = codeOut[0].equals(sortedCodes);
                boolean insOk = insOut[0].equals(codes);

                // what does the "raw" iteration order look like WITHOUT the extra work?
                boolean rawCodeOrder = new ArrayList<>(map.keySet()).equals(sortedCodes);
                boolean rawInsOrder = new ArrayList<>(map.keySet()).equals(codes);

                System.out.printf("%-14s %10.2f %14.2f %16.1f %16.2f %10s %10s   raw iteration: code-order=%s insertion-order=%s%n",
                        name, buildMs, lookupMs, lookupMs * 1e6 / LOOKUPS, codeReportMs, codeOk, insOk, rawCodeOrder, rawInsOrder);
                csv.printf("%d,%s,build_ms,%.4f%n", n, name, buildMs);
                csv.printf("%d,%s,lookups_100k_ms,%.4f%n", n, name, lookupMs);
                csv.printf("%d,%s,ns_per_lookup,%.2f%n", n, name, lookupMs * 1e6 / LOOKUPS);
                csv.printf("%d,%s,code_report_ms,%.4f%n", n, name, codeReportMs);
                csv.printf("%d,%s,insertion_report_ms,%.4f%n", n, name, insReportMs);
                System.out.printf("%-14s    insertion-order report: %.2f ms%n", "", insReportMs);
            }

            // ---------- alternative: sorted array + Arrays.binarySearch ----------
            Medicine[] arr = new Medicine[n];
            for (int i = 0; i < n; i++) arr[i] = new Medicine(sortedCodes.get(i), "Drug-" + sortedCodes.get(i), 100);
            Comparator<Medicine> byCode = Comparator.comparing(Medicine::code);
            double buildArr = timeIt(() -> () -> {
                Medicine[] a = new Medicine[n];
                for (int i = 0; i < n; i++) a[i] = new Medicine(codes.get(i), "Drug-" + codes.get(i), 100);
                Arrays.sort(a, byCode); sink = a.length; });
            double lookArr = timeIt(() -> () -> { long h = 0;
                for (String p : probes) { int i = Arrays.binarySearch(arr, new Medicine(p, null, 0), byCode); h += arr[i].stock(); } sink = h; });
            double repArr = timeIt(() -> () -> { long h = 0; for (Medicine m : arr) h += m.code().length(); sink = h; });
            System.out.printf("%-14s %10.2f %14.2f %16.1f %16.2f   (sorted array: build = sort)%n", "sorted array", buildArr, lookArr, lookArr * 1e6 / LOOKUPS, repArr);
            csv.printf("%d,sorted array,build_ms,%.4f%n", n, buildArr);
            csv.printf("%d,sorted array,lookups_100k_ms,%.4f%n", n, lookArr);
            csv.printf("%d,sorted array,ns_per_lookup,%.2f%n", n, lookArr * 1e6 / LOOKUPS);
            csv.printf("%d,sorted array,code_report_ms,%.4f%n", n, repArr);

            // cost of inserting 1,000 NEW medicines into each structure that is already holding n
            final int NEW = 1000;
            List<String> fresh = new ArrayList<>();
            for (int i = 0; i < NEW; i++) fresh.add(String.format("NEW%07d", i * 37 % 1000003));
            double insHash = timeIt(() -> { Map<String, Medicine> m = build(new HashMap<>(), codes); return () -> { for (String c : fresh) m.put(c, new Medicine(c, "x", 1)); sink = m.size(); }; });
            double insTree = timeIt(() -> { Map<String, Medicine> m = build(new TreeMap<>(), codes); return () -> { for (String c : fresh) m.put(c, new Medicine(c, "x", 1)); sink = m.size(); }; });
            double insArr = timeIt(() -> { Medicine[] a = Arrays.copyOf(arr, n + NEW); int[] size = {n}; return () -> {
                for (String c : fresh) { Medicine m = new Medicine(c, "x", 1); int p = Arrays.binarySearch(a, 0, size[0], m, byCode); p = -p - 1;
                    System.arraycopy(a, p, a, p + 1, size[0] - p); a[p] = m; size[0]++; } sink = size[0]; }; });
            System.out.printf("Insert 1,000 new medicines into structure holding %,d:  HashMap %.3f ms | TreeMap %.3f ms | sorted array %.3f ms%n", n, insHash, insTree, insArr);
            csv.printf("%d,HashMap,insert_1000_ms,%.4f%n", n, insHash);
            csv.printf("%d,TreeMap,insert_1000_ms,%.4f%n", n, insTree);
            csv.printf("%d,sorted array,insert_1000_ms,%.4f%n", n, insArr);
        }

        // ---------- approximate memory (n = 100,000), single measurement after gc ----------
        System.out.println("\nApproximate retained memory for 100,000 medicines (heap used after building, incl. the same Medicine/String objects):");
        List<String> codes = randomCodes(100_000, 99);
        long base = usedHeap();
        List<String> keep = new ArrayList<>(codes);                                // keys exist in all cases
        long baseKeys = usedHeap() - base;
        String[] names = {"HashMap", "TreeMap", "LinkedHashMap", "Hashtable"};
        Supplier<Map<String, Medicine>>[] makers = new Supplier[]{HashMap::new, TreeMap::new, LinkedHashMap::new, Hashtable::new};
        for (int k = 0; k < 4; k++) {
            long before = usedHeap();
            Map<String, Medicine> m = build(makers[k].get(), keep);
            long after = usedHeap();
            System.out.printf("  %-14s %8.2f MB  (%.0f bytes per entry, excluding key strings)%n", names[k], (after - before) / 1048576.0, (after - before) / 100000.0);
            csv.printf("100000,%s,memory_MB,%.3f%n", names[k], (after - before) / 1048576.0);
            sink = m.size();
        }
        csv.close();
        System.out.println("\nRaw data written to q4_results.csv");
    }
}

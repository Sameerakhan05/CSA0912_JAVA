import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Q2 - ArrayList vs LinkedList vs ArrayDeque (and a plain array with manual shifting)
 * for a music playlist editor and an "Up Next" queue.
 * Results (average of RUNS timed runs, setup excluded) are printed and saved to q2_results.csv.
 */
public class ListBenchmark {

    static final int RUNS = 5;
    static volatile long sink;
    static PrintWriter csv;

    record Song(int id, String title) {}

    static Song[] songs(int n) {
        Song[] s = new Song[n];
        for (int i = 0; i < n; i++) s[i] = new Song(i, "Song" + i);
        return s;
    }

    /** average milliseconds of 'body' over RUNS runs; 'setup' builds a fresh structure each run (not timed). */
    static <T> double time(Supplier<T> setup, Consumer<T> body) {
        for (int w = 0; w < 2; w++) body.accept(setup.get());          // warm-up
        double total = 0;
        for (int r = 0; r < RUNS; r++) {
            T s = setup.get();
            long t0 = System.nanoTime();
            body.accept(s);
            total += (System.nanoTime() - t0) / 1e6;
        }
        return total / RUNS;
    }

    static List<Song> filled(List<Song> l, Song[] src) { l.addAll(Arrays.asList(src)); return l; }

    static void report(String op, int n, String ds, double ms) {
        System.out.printf("%-26s n=%-8d %-12s %12.3f ms%n", op, n, ds, ms);
        csv.printf("%s,%d,%s,%.4f%n", op, n, ds, ms);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + System.getProperty("java.version"));
        csv = new PrintWriter(new FileWriter("q2_results.csv"));
        csv.println("operation,size,structure,time_ms");

        int[] sizes = {10_000, 50_000, 100_000};
        final int OPS = 2_000;                      // number of front/middle/get operations per run
        Random rnd = new Random(42);

        for (int n : sizes) {
            Song[] src = songs(n);
            Song extra = new Song(-1, "New");
            int[] idx = new int[OPS];
            for (int i = 0; i < OPS; i++) idx[i] = rnd.nextInt(n);

            // 1. add at end : build a playlist of n songs by appending
            report("add at end (n adds)", n, "ArrayList", time(() -> new ArrayList<Song>(), l -> { for (Song s : src) l.add(s); }));
            report("add at end (n adds)", n, "LinkedList", time(() -> new LinkedList<Song>(), l -> { for (Song s : src) l.add(s); }));
            report("add at end (n adds)", n, "ArrayDeque", time(() -> new ArrayDeque<Song>(), l -> { for (Song s : src) l.addLast(s); }));

            // 2. insert at front
            report("insert at front", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.add(0, extra); }));
            report("insert at front", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.add(0, extra); }));
            report("insert at front", n, "ArrayDeque", time(() -> new ArrayDeque<>(Arrays.asList(src)), l -> { for (int i = 0; i < OPS; i++) l.addFirst(extra); }));

            // 3. insert in the middle
            report("insert in middle", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.add(l.size() / 2, extra); }));
            report("insert in middle", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.add(l.size() / 2, extra); }));

            // 4. remove from front / middle
            report("remove from front", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.remove(0); }));
            report("remove from front", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.remove(0); }));
            report("remove from front", n, "ArrayDeque", time(() -> new ArrayDeque<>(Arrays.asList(src)), l -> { for (int i = 0; i < OPS; i++) l.removeFirst(); }));
            report("remove from middle", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.remove(l.size() / 2); }));
            report("remove from middle", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { for (int i = 0; i < OPS; i++) l.remove(l.size() / 2); }));

            // 5. get k-th element (random access)
            report("get k-th (2000 gets)", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { long h = 0; for (int k : idx) h += l.get(k).id(); sink = h; }));
            report("get k-th (2000 gets)", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { long h = 0; for (int k : idx) h += l.get(k).id(); sink = h; }));

            // 6. iterate the whole list
            report("iterate all", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> { long h = 0; for (Song s : l) h += s.id(); sink = h; }));
            report("iterate all", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> { long h = 0; for (Song s : l) h += s.id(); sink = h; }));
            report("iterate all", n, "ArrayDeque", time(() -> new ArrayDeque<>(Arrays.asList(src)), l -> { long h = 0; for (Song s : l) h += s.id(); sink = h; }));

            // 7. plain array with manual shifting (alternative concept)
            report("insert at front", n, "plain array", time(() -> { Song[] a = new Song[n + OPS]; System.arraycopy(src, 0, a, 0, n); return new Object[]{a, n}; },
                    st -> { Song[] a = (Song[]) st[0]; int size = (Integer) st[1];
                            for (int i = 0; i < OPS; i++) { for (int j = size; j > 0; j--) a[j] = a[j - 1]; a[0] = extra; size++; } sink = size; }));
            report("insert in middle", n, "plain array", time(() -> { Song[] a = new Song[n + OPS]; System.arraycopy(src, 0, a, 0, n); return new Object[]{a, n}; },
                    st -> { Song[] a = (Song[]) st[0]; int size = (Integer) st[1];
                            for (int i = 0; i < OPS; i++) { int p = size / 2; for (int j = size; j > p; j--) a[j] = a[j - 1]; a[p] = extra; size++; } sink = size; }));
            report("get k-th (2000 gets)", n, "plain array", time(() -> src, a -> { long h = 0; for (int k : idx) h += a[k].id(); sink = h; }));
            System.out.println();
        }

        // ---------- "Up Next" queue: add at both ends, remove from front ----------
        System.out.println("=== UP NEXT QUEUE: 100,000 mixed operations (addFirst / addLast / removeFirst) on a queue pre-filled with n songs ===");
        final int MIX = 100_000;
        for (int n : new int[]{1_000, 10_000, 50_000}) {
            Song[] src = songs(n);
            Song extra = new Song(-1, "New");
            report("UpNext mixed ops", n, "ArrayList", time(() -> filled(new ArrayList<>(), src), l -> {
                for (int i = 0; i < MIX; i++) { int m = i % 3; if (m == 0) l.add(0, extra); else if (m == 1) l.add(extra); else l.remove(0); } }));
            report("UpNext mixed ops", n, "LinkedList", time(() -> filled(new LinkedList<>(), src), l -> {
                for (int i = 0; i < MIX; i++) { int m = i % 3; if (m == 0) l.addFirst(extra); else if (m == 1) l.addLast(extra); else l.removeFirst(); } }));
            report("UpNext mixed ops", n, "ArrayDeque", time(() -> new ArrayDeque<>(Arrays.asList(src)), l -> {
                for (int i = 0; i < MIX; i++) { int m = i % 3; if (m == 0) l.addFirst(extra); else if (m == 1) l.addLast(extra); else l.removeFirst(); } }));
            System.out.println();
        }
        csv.close();
        System.out.println("Raw data written to q2_results.csv");
    }
}

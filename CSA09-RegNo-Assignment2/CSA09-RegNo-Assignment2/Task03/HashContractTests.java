import java.util.*;

/**
 * Q3 - hashCode / equals contract experiments.
 * Each test prints PREDICTION, OBSERVED and a verdict.
 */
public class HashContractTests {

    /** The junior developer's class: equals() on regNo, NO hashCode(). */
    static class BadStudent {
        final String regNo, name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof BadStudent)) return false;
            return regNo.equals(((BadStudent) o).regNo);
        }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Corrected class: hashCode() consistent with equals(), fields final. */
    static class GoodStudent {
        final String regNo, name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoodStudent)) return false;
            return regNo.equals(((GoodStudent) o).regNo);
        }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public String toString() { return regNo + " " + name; }
    }

    /** Hash is correct but the key field is MUTABLE (test 3). */
    static class MutableStudent {
        String regNo; final String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) {
            return o instanceof MutableStudent && regNo.equals(((MutableStudent) o).regNo);
        }
        @Override public int hashCode() { return regNo.hashCode(); }
    }

    static void verdict(String test, int predicted, int observed) {
        System.out.printf("   PREDICTED size = %d | OBSERVED size = %d | %s%n%n", predicted, observed,
                predicted == observed ? "prediction CORRECT" : "prediction WRONG -> contract violation visible");
    }

    public static void main(String[] args) {
        System.out.println("Java " + System.getProperty("java.version") + "\n");

        // ---------------- Test 1: set that APPEARS to work ----------------
        System.out.println("TEST 1: HashSet<BadStudent>, 3 DIFFERENT students, then the SAME object re-added (re-scan of the same card object)");
        System.out.println("   Reason for prediction: same object => same identity hashCode => same bucket => equals() true => 3 stays 3.");
        Set<BadStudent> t1 = new HashSet<>();
        BadStudent a = new BadStudent("192311001", "Arun");
        t1.add(a); t1.add(new BadStudent("192311002", "Divya")); t1.add(new BadStudent("192311003", "Kiran"));
        t1.add(a);                                   // identical reference
        verdict("t1", 3, t1.size());

        // ---------------- Test 2: set that EXPOSES duplicates ----------------
        System.out.println("TEST 2: HashSet<BadStudent>, 1000 scans of the SAME student (a new object per scan)");
        System.out.println("   Reason for prediction: equals() says all equal, but Object.hashCode() differs per object, so HashSet");
        System.out.println("   looks in different buckets and never even calls equals(). Expected ~1000 entries instead of 1.");
        Set<BadStudent> t2 = new HashSet<>();
        for (int i = 0; i < 1000; i++) t2.add(new BadStudent("192311001", "Arun"));
        verdict("t2", 1000, t2.size());
        BadStudent probe = new BadStudent("192311001", "Arun");
        System.out.println("   contains(new BadStudent(\"192311001\")) = " + t2.contains(probe)
                + "   (equals says yes, but HashSet looks in the wrong bucket)");
        Set<GoodStudent> t2g = new HashSet<>();
        for (int i = 0; i < 1000; i++) t2g.add(new GoodStudent("192311001", "Arun"));
        System.out.println("   Same test with the corrected class: size = " + t2g.size()
                + ", contains = " + t2g.contains(new GoodStudent("192311001", "x")) + "\n");

        // ---------------- Test 3 (own): mutate the key field after insertion ----------------
        System.out.println("TEST 3 (own): MutableStudent with a correct hashCode; regNo is changed AFTER insertion");
        System.out.println("   Reason for prediction: size stays 1, but the entry now sits in the bucket of the OLD hash, so");
        System.out.println("   contains(new regNo) = false (searches new bucket) and contains(old regNo) = false (equals fails).");
        Set<MutableStudent> t3 = new HashSet<>();
        MutableStudent m = new MutableStudent("192311001", "Arun");
        t3.add(m);
        m.regNo = "192311999";                       // field used in hashCode() changed
        System.out.println("   size = " + t3.size());
        System.out.println("   contains(regNo=192311999 (new value)) = " + t3.contains(new MutableStudent("192311999", "x")));
        System.out.println("   contains(regNo=192311001 (old value)) = " + t3.contains(new MutableStudent("192311001", "x")));
        System.out.println("   contains(m itself)                    = " + t3.contains(m));
        System.out.println("   remove(m)                             = " + t3.remove(m) + "   (m is stranded in the old bucket - it cannot be removed)");
        verdict("t3", 1, t3.size());
        t3.add(new MutableStudent("192311999", "Arun-again"));
        System.out.println("   Re-scanning the student under the new regNo: size = " + t3.size()
                + " -> the same student now exists twice (a 'ghost' entry plus the new one)\n");

        // ---------------- Test 4 (own): TreeSet inconsistent with equals ----------------
        System.out.println("TEST 4 (own): TreeSet whose comparator compares only the first 5 characters of regNo (inconsistent with equals)");
        System.out.println("   Prediction: 192311001 and 192311002 compare as 0 => TreeSet keeps only one, although equals() says they differ => size 1.");
        TreeSet<GoodStudent> t4 = new TreeSet<>(Comparator.comparing((GoodStudent s) -> s.regNo.substring(0, 5)));
        t4.add(new GoodStudent("192311001", "Arun")); t4.add(new GoodStudent("192311002", "Divya"));
        verdict("t4", 1, t4.size());

        // ---------------- Alternatives: TreeSet(Comparator on regNo) and HashMap<String,Student> ----------------
        System.out.println("ALTERNATIVES - de-duplicating n scans (each of n/3 unique ids scanned 3 times), average of 5 runs:");
        System.out.printf("%10s %18s %18s %18s%n", "scans", "HashSet (ms)", "TreeSet (ms)", "HashMap (ms)");
        for (int unique : new int[]{10_000, 100_000, 400_000}) {
            String[] ids = new String[unique * 3];
            for (int i = 0; i < ids.length; i++) ids[i] = "19231" + String.format("%06d", ((long) i * 7919) % unique);
            double h = 0, t = 0, mp = 0;
            for (int run = 0; run < 7; run++) {                 // first 2 runs = warm-up
                long s0 = System.nanoTime();
                Set<GoodStudent> hs = new HashSet<>();
                for (String id : ids) hs.add(new GoodStudent(id, "n"));
                long s1 = System.nanoTime();
                TreeSet<GoodStudent> ts = new TreeSet<>(Comparator.comparing((GoodStudent s) -> s.regNo));
                for (String id : ids) ts.add(new GoodStudent(id, "n"));
                long s2 = System.nanoTime();
                Map<String, GoodStudent> hm = new HashMap<>();
                for (String id : ids) hm.putIfAbsent(id, new GoodStudent(id, "n"));
                long s3 = System.nanoTime();
                if (run >= 2) { h += (s1 - s0) / 1e6; t += (s2 - s1) / 1e6; mp += (s3 - s2) / 1e6; }
                if (hs.size() != unique || ts.size() != unique || hm.size() != unique) throw new AssertionError("wrong size");
            }
            System.out.printf("%10d %18.2f %18.2f %18.2f%n", ids.length, h / 5, t / 5, mp / 5);
        }
    }
}

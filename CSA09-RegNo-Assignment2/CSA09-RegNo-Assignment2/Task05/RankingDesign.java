import java.util.*;
import java.util.function.*;

/**
 * Q5 - Generic ranking component derived from the "consistent comparison" principle.
 * Contains: (1) the Comparator-chain design, (2) a bounded generic Leaderboard<T>,
 * (3) wildcard use, (4) a brute-force contract checker, (5) faulty comparators that break the
 * principle, (6) alternatives: Comparable and three stable sorts, with comparison counts / timings.
 */
public class RankingDesign {

    record Team(String name, int points, int goalDiff) {}

    // ---------------- (1) the correct, reusable comparator chain ----------------
    static final Comparator<Team> BY_RANK =
            Comparator.comparingInt(Team::points).reversed()                            // 1st key: points high -> low
                      .thenComparing(Comparator.comparingInt(Team::goalDiff).reversed()) // 2nd key: goal difference high -> low
                      .thenComparing(Team::name);                                        // 3rd key: name A-Z

    // ---------------- (2) generic, reusable component ----------------
    /** Works for ANY type T; the ordering is supplied from outside, so the same class ranks teams, players, colleges... */
    static class Leaderboard<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;          // ? super T : a Comparator<Object>/Comparator<Person> can rank Students
        Leaderboard(Comparator<? super T> order) { this.order = order; }
        void add(T t) { items.add(t); }
        void addAll(Collection<? extends T> more) { items.addAll(more); }   // ? extends T : accept List<Student> into Leaderboard<Person>
        List<T> ranking() { List<T> c = new ArrayList<>(items); c.sort(order); return c; }
        List<T> top(int k) { List<T> r = ranking(); return r.subList(0, Math.min(k, r.size())); }
    }

    /** Bounded generic method: only works for types that know their own natural order. */
    static <T extends Comparable<? super T>> List<T> rankNatural(Collection<? extends T> items) {
        List<T> c = new ArrayList<>(items); Collections.sort(c); return c;
    }

    /** Team with a NATURAL order (alternative design: Comparable<Team>) */
    record RankedTeam(String name, int points, int goalDiff) implements Comparable<RankedTeam> {
        public int compareTo(RankedTeam o) {
            int c = Integer.compare(o.points, points);
            if (c != 0) return c;
            c = Integer.compare(o.goalDiff, goalDiff);
            if (c != 0) return c;
            return name.compareTo(o.name);
        }
    }

    // ---------------- (4) brute-force verification of the principle ----------------
    /** checks antisymmetry, transitivity and equal-consistency over ALL pairs / triples of the sample */
    static <T> String check(List<T> sample, Comparator<T> c) {
        int n = sample.size(), antisym = 0, trans = 0;
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) {
            if (Integer.signum(c.compare(sample.get(i), sample.get(j))) != -Integer.signum(c.compare(sample.get(j), sample.get(i)))) antisym++;
        }
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) for (int k = 0; k < n; k++) {
            T a = sample.get(i), b = sample.get(j), d = sample.get(k);
            if (c.compare(a, b) > 0 && c.compare(b, d) > 0 && c.compare(a, d) <= 0) trans++;
        }
        return "antisymmetry violations=" + antisym + ", transitivity violations=" + trans + (antisym + trans == 0 ? "  => CONSISTENT" : "  => BROKEN");
    }

    // ---------------- (5) faulty comparators ----------------
    static final Comparator<Team> FAULTY_SUBTRACT = (a, b) -> b.points() - a.points();            // overflow for large/negative values
    static final Comparator<Team> FAULTY_NO_TIEBREAK = Comparator.comparingInt(Team::points).reversed()
                                                          .thenComparing(Comparator.comparingInt(Team::goalDiff).reversed());  // ignores name

    // counting wrapper
    static class Counting<T> implements Comparator<T> {
        final Comparator<T> inner; long count;
        Counting(Comparator<T> in) { inner = in; }
        public int compare(T a, T b) { count++; return inner.compare(a, b); }
    }

    public static void main(String[] args) {
        System.out.println("Java " + System.getProperty("java.version") + "\n");

        // ---- TEST SET 1: many ties at every level ----
        List<Team> t1 = new ArrayList<>(List.of(
            new Team("Panthers", 9, 4), new Team("Eagles", 9, 6), new Team("Cobras", 6, 1), new Team("Bisons", 9, 4),
            new Team("Falcons", 6, 1), new Team("Aces", 6, 3), new Team("Dragons", 3, -2), new Team("Comets", 3, -2),
            new Team("Bulls", 3, 0), new Team("Zebras", 9, 6), new Team("Wolves", 0, -9), new Team("Archers", 6, 1)));
        Collections.shuffle(t1, new Random(1));
        Leaderboard<Team> lb = new Leaderboard<>(BY_RANK);
        lb.addAll(t1);
        System.out.println("TEST SET 1 (ties at every level) - correct comparator chain:");
        lb.ranking().forEach(t -> System.out.println("   " + t));
        System.out.println("   top 3 = " + lb.top(3));
        System.out.println("   contract check: " + check(t1, BY_RANK));
        System.out.println("   same result when input is reversed (stability / determinism): "
                + equalsAfterShuffle(t1, BY_RANK));

        // ---- TEST SET 2: data deliberately designed to break faulty comparators ----
        List<Team> t2 = new ArrayList<>(List.of(
            new Team("Huge", Integer.MAX_VALUE, 0),            // b.points - a.points overflows against negative values
            new Team("Normal", 5, 0),
            new Team("Penalised", -2_000_000_000, 0),
            new Team("TieA", 7, 3), new Team("TieB", 7, 3), new Team("TieC", 7, 3)));
        System.out.println("\nTEST SET 2 (designed to break faulty comparators):");
        System.out.println("   FAULTY_SUBTRACT   : " + check(t2, FAULTY_SUBTRACT));
        System.out.println("      compare(Huge, Penalised) = " + FAULTY_SUBTRACT.compare(t2.get(0), t2.get(2))
                + "  (Huge has MORE points so it should rank first => negative expected; overflow gives positive)");
        System.out.println("   FAULTY_NO_TIEBREAK: " + check(t2, FAULTY_NO_TIEBREAK)
                + "  (still transitive, but TieA/TieB/TieC compare as equal => their order depends on input order)");
        List<Team> in1 = new ArrayList<>(t2), in2 = new ArrayList<>(t2); Collections.reverse(in2);
        in1.sort(FAULTY_NO_TIEBREAK); in2.sort(FAULTY_NO_TIEBREAK);
        System.out.println("      forward input  -> " + names(in1));
        System.out.println("      reversed input -> " + names(in2) + "   (different! ranking is not deterministic)");
        in1 = new ArrayList<>(t2); in2 = new ArrayList<>(t2); Collections.reverse(in2);
        in1.sort(BY_RANK); in2.sort(BY_RANK);
        System.out.println("   BY_RANK (correct) : " + check(t2, BY_RANK));
        System.out.println("      forward input  -> " + names(in1));
        System.out.println("      reversed input -> " + names(in2) + "   (identical)");
        // TimSort may even detect the inconsistency
        try {
            Random r = new Random(5); List<Team> big = new ArrayList<>();
            for (int i = 0; i < 2000; i++) big.add(new Team("T" + i, r.nextBoolean() ? Integer.MAX_VALUE - r.nextInt(10) : -2_000_000_000 + r.nextInt(10), 0));
            big.sort(FAULTY_SUBTRACT);
            System.out.println("   sort with FAULTY_SUBTRACT on 2000 teams: no exception, but result is wrong (e.g. first = " + big.get(0) + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("   sort with FAULTY_SUBTRACT on 2000 teams threw: " + e.getMessage());
        }

        // ---- Comparable alternative + bounded generic method ----
        List<RankedTeam> rt = new ArrayList<>();
        for (Team t : t1) rt.add(new RankedTeam(t.name(), t.points(), t.goalDiff()));
        List<RankedTeam> natural = rankNatural(rt);
        System.out.println("\nComparable<RankedTeam> + rankNatural(...) gives same order as comparator chain: " + sameOrder(lb.ranking(), natural));

        // ---- alternatives: single chained sort vs three stable sorts; comparison counts + time ----
        System.out.println("\nALTERNATIVE: one Comparator-chain sort  vs  three stable sorts (name, then goalDiff, then points)");
        System.out.printf("%10s %18s %18s %14s %14s %10s%n", "n", "chain comparisons", "3-sort comparisons", "chain ms", "3-sort ms", "same?");
        for (int n : new int[]{10_000, 100_000, 1_000_000}) {
            Random r = new Random(11); List<Team> data = new ArrayList<>(n);
            for (int i = 0; i < n; i++) data.add(new Team("Team" + r.nextInt(n), r.nextInt(30), r.nextInt(20) - 10));
            // counts
            Counting<Team> cc = new Counting<>(BY_RANK); List<Team> a = new ArrayList<>(data); a.sort(cc);
            Counting<Team> c1 = new Counting<>(Comparator.comparing(Team::name));
            Counting<Team> c2 = new Counting<>(Comparator.comparingInt(Team::goalDiff).reversed());
            Counting<Team> c3 = new Counting<>(Comparator.comparingInt(Team::points).reversed());
            List<Team> b = new ArrayList<>(data); b.sort(c1); b.sort(c2); b.sort(c3);
            // times (avg of 5 after 2 warm-ups)
            double tc = 0, t3 = 0;
            for (int run = 0; run < 7; run++) {
                List<Team> x = new ArrayList<>(data); long s0 = System.nanoTime(); x.sort(BY_RANK); long s1 = System.nanoTime();
                List<Team> y = new ArrayList<>(data);
                y.sort(Comparator.comparing(Team::name)); y.sort(Comparator.comparingInt(Team::goalDiff).reversed()); y.sort(Comparator.comparingInt(Team::points).reversed());
                long s2 = System.nanoTime();
                if (run >= 2) { tc += (s1 - s0) / 1e6; t3 += (s2 - s1) / 1e6; }
            }
            System.out.printf("%10d %18d %18d %14.1f %14.1f %10s%n", n, cc.count, c1.count + c2.count + c3.count, tc / 5, t3 / 5, a.equals(b));
        }
    }

    static boolean equalsAfterShuffle(List<Team> in, Comparator<Team> c) {
        List<Team> a = new ArrayList<>(in), b = new ArrayList<>(in); Collections.reverse(b); a.sort(c); b.sort(c); return a.equals(b);
    }
    static <A, B> boolean sameOrder(List<Team> x, List<RankedTeam> y) {
        for (int i = 0; i < x.size(); i++) if (!x.get(i).name().equals(y.get(i).name())) return false; return true;
    }
    static String names(List<Team> l) { StringBuilder s = new StringBuilder(); for (Team t : l) s.append(t.name()).append(' '); return s.toString().trim(); }
}

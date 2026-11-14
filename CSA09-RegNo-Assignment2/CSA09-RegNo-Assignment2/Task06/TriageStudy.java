import java.io.*;
import java.util.*;

/**
 * Q6 - Priority, order and fairness in a queue (hospital triage).
 * Parts: (1) hand-verifiable 8-patient check, (2) why printing a PriorityQueue is not treatment order,
 * (3) growth of add/poll cost, (4) alternatives (sorted LinkedList, 5 FIFO queues),
 * (5) starvation simulation + fair "aging" rule, (6) 5,000 patients/day deployment check.
 */
public class TriageStudy {

    record Patient(String name, int severity, long arrival) {}
    static final Comparator<Patient> STRICT =
            Comparator.comparingInt(Patient::severity).thenComparingLong(Patient::arrival);

    static volatile long sink;
    static PrintWriter csv;

    public static void main(String[] args) throws Exception {
        System.out.println("Java " + System.getProperty("java.version") + "\n");
        csv = new PrintWriter(new FileWriter("q6_results.csv"));
        csv.println("n,design,metric,value");

        // ---------------- (1) hand-verified instance ----------------
        String[][] in = {{"Ravi","3"},{"Meena","1"},{"John","3"},{"Asha","5"},{"Kumar","1"},{"Lata","2"},{"Divya","2"},{"Tom","4"}};
        PriorityQueue<Patient> pq = new PriorityQueue<>(STRICT);
        long c = 0;
        System.out.println("ARRIVALS (arrival no. : name severity): ");
        for (String[] p : in) { pq.add(new Patient(p[0], Integer.parseInt(p[1]), c)); System.out.print("  " + c + ":" + p[0] + " S" + p[1]); c++; }
        System.out.println("\n\nHAND-WORKED EXPECTED ORDER : Meena(S1) Kumar(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Tom(S4) Asha(S5)");
        System.out.println("printing the PriorityQueue directly : " + pq + "   <-- heap array order, NOT treatment order");
        StringBuilder sb = new StringBuilder();
        Patient p; while ((p = pq.poll()) != null) sb.append(p.name()).append("(S").append(p.severity()).append(") ");
        System.out.println("order produced by poll() repeatedly : " + sb.toString().trim());
        System.out.println("(a plain FIFO queue would give   : " + fifo(in) + ")\n");

        // ---------------- (3) growth: add and poll ----------------
        System.out.println("GROWTH - PriorityQueue (binary heap): prediction add = O(log n), poll = O(log n)  (average of 5 runs)");
        System.out.printf("%10s %14s %14s %16s %16s%n", "n", "add all (ms)", "poll all (ms)", "ns / add", "ns / poll");
        Random rnd = new Random(3);
        for (int n : new int[]{10_000, 100_000, 1_000_000}) {
            Patient[] arr = new Patient[n];
            for (int i = 0; i < n; i++) arr[i] = new Patient("P" + i, 1 + rnd.nextInt(5), i);
            double add = 0, poll = 0;
            for (int run = 0; run < 7; run++) {
                PriorityQueue<Patient> q = new PriorityQueue<>(STRICT);
                long s0 = System.nanoTime(); for (Patient x : arr) q.add(x); long s1 = System.nanoTime();
                long h = 0; while (!q.isEmpty()) h += q.poll().arrival(); long s2 = System.nanoTime(); sink = h;
                if (run >= 2) { add += (s1 - s0) / 1e6; poll += (s2 - s1) / 1e6; }
            }
            add /= 5; poll /= 5;
            System.out.printf("%10d %14.2f %14.2f %16.1f %16.1f%n", n, add, poll, add * 1e6 / n, poll * 1e6 / n);
            csv.printf("%d,PriorityQueue,add_ns,%.2f%n", n, add * 1e6 / n);
            csv.printf("%d,PriorityQueue,poll_ns,%.2f%n", n, poll * 1e6 / n);
        }

        // ---------------- (4) alternatives ----------------
        System.out.println("\nALTERNATIVES at equal sizes (total time to admit n patients then serve all, average of 3 runs):");
        System.out.printf("%8s %20s %24s %22s%n", "n", "PriorityQueue (ms)", "sorted LinkedList (ms)", "5 FIFO queues (ms)");
        for (int n : new int[]{5_000, 20_000, 50_000}) {
            Patient[] arr = new Patient[n];
            for (int i = 0; i < n; i++) arr[i] = new Patient("P" + i, 1 + rnd.nextInt(5), i);
            double a = 0, b = 0, d = 0;
            for (int run = 0; run < 5; run++) {
                long s0 = System.nanoTime();
                PriorityQueue<Patient> q = new PriorityQueue<>(STRICT); for (Patient x : arr) q.add(x); long h = 0; while (!q.isEmpty()) h += q.poll().arrival();
                long s1 = System.nanoTime();
                LinkedList<Patient> ll = new LinkedList<>();                      // kept in sorted order: insert at correct position
                for (Patient x : arr) { ListIterator<Patient> it = ll.listIterator(); while (it.hasNext()) { if (STRICT.compare(it.next(), x) > 0) { it.previous(); break; } } it.add(x); }
                while (!ll.isEmpty()) h += ll.removeFirst().arrival();
                long s2 = System.nanoTime();
                @SuppressWarnings("unchecked") ArrayDeque<Patient>[] five = new ArrayDeque[6];
                for (int i = 1; i <= 5; i++) five[i] = new ArrayDeque<>();
                for (Patient x : arr) five[x.severity()].addLast(x);
                for (int served = 0; served < n; served++) { for (int s = 1; s <= 5; s++) if (!five[s].isEmpty()) { h += five[s].pollFirst().arrival(); break; } }
                long s3 = System.nanoTime(); sink = h;
                if (run >= 2) { a += (s1 - s0) / 1e6; b += (s2 - s1) / 1e6; d += (s3 - s2) / 1e6; }
            }
            System.out.printf("%8d %20.2f %24.2f %22.2f%n", n, a / 3, b / 3, d / 3);
            csv.printf("%d,PriorityQueue,total_ms,%.3f%n%d,sorted LinkedList,total_ms,%.3f%n%d,5 FIFO queues,total_ms,%.3f%n", n, a / 3, n, b / 3, n, d / 3);
        }

        // ---------------- (5) starvation + aging ----------------
        System.out.println("\nSTARVATION SIMULATION: one severity-5 patient arrives at tick 0; every tick 6 new severity-1/2 patients arrive per 5 ticks (1.2 per tick) while the doctor serves 1 per tick (overloaded ED).");
        for (String mode : new String[]{"strict priority", "aging (W=50)"}) {
            final int W = 50, TICKS = 20_000;
            // aging key = arrival + W*(severity-1): a patient of severity s behaves as if he had arrived (s-1)*W ticks later.
            Comparator<Patient> cmp = mode.startsWith("strict") ? STRICT
                    : Comparator.comparingLong((Patient x) -> x.arrival() + (long) W * (x.severity() - 1)).thenComparingLong(Patient::arrival);
            PriorityQueue<Patient> q = new PriorityQueue<>(cmp);
            Random r = new Random(9);
            q.add(new Patient("OLD-S5", 5, 0));
            long servedAtTick = -1, maxWaitS1 = 0, served1 = 0; long arrivals = 0;
            for (long t = 1; t <= TICKS; t++) {
                int newcomers = (t % 5 == 0) ? 2 : 1;                      // 6 per 5 ticks
                for (int k = 0; k < newcomers; k++) q.add(new Patient("N" + (arrivals++), 1 + r.nextInt(2), t));
                Patient s = q.poll();
                if (s.name().equals("OLD-S5")) { servedAtTick = t; }
                if (s.severity() == 1) { maxWaitS1 = Math.max(maxWaitS1, t - s.arrival()); served1++; }
            }
            System.out.printf("  %-16s: severity-5 patient %s; longest wait of any severity-1 patient = %d ticks; queue length at end = %d%n",
                    mode, servedAtTick < 0 ? "NEVER served in " + TICKS + " ticks" : "served at tick " + servedAtTick, maxWaitS1, q.size());
            csv.printf("20000,%s,sev5_served_tick,%d%n", mode, servedAtTick);
            csv.printf("20000,%s,max_wait_sev1_ticks,%d%n", mode, maxWaitS1);
        }

        // ---------------- (6) city hospital ----------------
        System.out.println("\nDEPLOYMENT CHECK: 5,000 patients/day = " + String.format("%.2f", 5000 / 86400.0) + " arrivals per second.");
        PriorityQueue<Patient> day = new PriorityQueue<>(STRICT);
        long t0 = System.nanoTime();
        for (int i = 0; i < 5000; i++) day.add(new Patient("P" + i, 1 + rnd.nextInt(5), i));
        while (!day.isEmpty()) sink += day.poll().arrival();
        System.out.printf("  admitting and serving all 5,000 patients took %.3f ms in total%n", (System.nanoTime() - t0) / 1e6);
        csv.close();
        System.out.println("\nRaw data written to q6_results.csv");
    }

    static String fifo(String[][] in) { StringBuilder s = new StringBuilder(); for (String[] p : in) s.append(p[0]).append("(S").append(p[1]).append(") "); return s.toString().trim(); }
}

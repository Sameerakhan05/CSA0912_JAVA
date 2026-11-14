const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, ImageRun, HeadingLevel,
  AlignmentType, LevelFormat, WidthType, BorderStyle, ShadingType, PageBreak, Footer, PageNumber,
} = require("docx");

const ROOT = "/home/claude/CSA09-RegNo-Assignment2/";
const A = ROOT + "report_assets/";
const data = JSON.parse(fs.readFileSync(A + "data.json"));
const W = 9638; // content width (A4, 2 cm margins)
const FONT = "Calibri";

// ---------- helpers ----------
const run = (t, o = {}) => new TextRun({ text: t, font: FONT, size: 22, ...o });
const P = (t, o = {}) => new Paragraph({ spacing: { after: 120, line: 276 }, ...o,
  children: Array.isArray(t) ? t : [run(t)] });
const B = (t) => new Paragraph({ numbering: { reference: "bul", level: 0 }, spacing: { after: 60 },
  children: Array.isArray(t) ? t : [run(t)] });
const bold = (t) => run(t, { bold: true });
const H1 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun({ text: t, font: FONT })] });
const H2 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun({ text: t, font: FONT })] });
const H3 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun({ text: t, font: FONT })] });
const gap = () => new Paragraph({ children: [] });
const pb = () => new Paragraph({ children: [new PageBreak()] });

const bd = { style: BorderStyle.SINGLE, size: 4, color: "999999" };
const borders = { top: bd, bottom: bd, left: bd, right: bd };
function tbl(headers, rows, fr) {
  const tot = fr.reduce((a, b) => a + b, 0);
  const widths = fr.map((f) => Math.floor((f / tot) * W));
  widths[widths.length - 1] += W - widths.reduce((a, b) => a + b, 0);
  const cell = (t, i, head) => new TableCell({
    borders, width: { size: widths[i], type: WidthType.DXA },
    shading: head ? { fill: "D9E2F3", type: ShadingType.CLEAR, color: "auto" } : undefined,
    margins: { top: 50, bottom: 50, left: 90, right: 90 },
    children: [new Paragraph({ children: [new TextRun({ text: String(t), font: FONT, size: 19, bold: head })] })],
  });
  return new Table({
    width: { size: W, type: WidthType.DXA }, columnWidths: widths,
    rows: [new TableRow({ tableHeader: true, children: headers.map((h, i) => cell(h, i, true)) }),
      ...rows.map((r) => new TableRow({ children: r.map((c, i) => cell(c, i, false)) }))],
  });
}
function code(text) {
  const lines = text.replace(/\t/g, "    ").split("\n");
  return new Table({
    width: { size: W, type: WidthType.DXA }, columnWidths: [W],
    rows: [new TableRow({ children: [new TableCell({
      borders, width: { size: W, type: WidthType.DXA },
      shading: { fill: "F4F4F4", type: ShadingType.CLEAR, color: "auto" },
      margins: { top: 80, bottom: 80, left: 120, right: 120 },
      children: lines.map((l) => new Paragraph({ spacing: { after: 0 },
        children: [new TextRun({ text: l === "" ? " " : l, font: "Consolas", size: 17 })] })),
    })] })],
  });
}
function img(file, wpx, hpx, caption) {
  return [new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120, after: 60 }, children: [
    new ImageRun({ type: "png", data: fs.readFileSync(A + file), transformation: { width: wpx, height: hpx },
      altText: { title: caption, description: caption, name: file } })] }),
    new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 160 },
      children: [run(caption, { italics: true, size: 19 })] })];
}
const shot = (what) => new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 80, after: 160 },
  border: { top: bd, bottom: bd, left: bd, right: bd },
  children: [run("[Paste your own screenshot here: " + what + "]", { italics: true, color: "666666", size: 19 })] });
const rd = (f) => fs.readFileSync(ROOT + f, "utf8").trim();
const f1 = (x) => x.toFixed(1), f2 = (x) => x.toFixed(2), f3 = (x) => x.toFixed(3);
const q2 = (op, s, n) => data.q2[op + "|" + s][n];

const C = []; // all content

// ================= COVER =================
C.push(gap(), gap(),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 }, children: [run("PROGRAMMING IN JAVA", { bold: true, size: 40 })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 }, children: [run("ASSIGNMENT 2 \u2013 CO2", { bold: true, size: 32 })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 400 }, children: [run("Strings, Collections Framework, hashCode\u2013equals, Generics, Comparable/Comparator", { size: 24 })] }),
  tbl(["Item", "Details"], [
    ["Student name", "<Your Name>"],
    ["Register number", "<RegisterNumber>"],
    ["Course", "CSA09 \u2013 Programming in Java"],
    ["Nature / marks", "Individual / 50 marks"],
    ["Java version used", "OpenJDK 21 (replace with the output of `java -version` on your machine)"],
    ["GitHub repository", "https://github.com/<your-username>/CSA09-<RegisterNumber>-Assignment2"],
    ["Submission date", "<date>"],
  ], [2, 6]),
  gap(),
  P([bold("Experimental method (applies to every task). "), run("Each timing is the average of 5 timed runs after 2 warm-up runs, using System.nanoTime(). Where useful, bytes allocated per thread (ThreadMXBean) and operation counts (comparison counters) are reported. Raw data for every table is in the *_results.csv file of the matching task folder in the repository. Measurements were taken on a single-core machine, so small absolute differences (below about 1 ms) are noisy; the conclusions rely on the growth trends.")]),
  pb());

// ================= Q1 =================
C.push(H1("Q1 \u2013 Building Text Under Load: String, StringBuilder or StringBuffer? (6 marks)"));
C.push(H2("1. Prediction (before implementation)"));
C.push(B("String is immutable, so every `s += x` creates a brand-new String and copies all characters collected so far. For n fields of length L the total characters copied are L(1+2+\u2026+n) = L\u00b7n(n+1)/2, i.e. O(n\u00b2), and n intermediate String objects are created."));
C.push(B("StringBuilder keeps one growing char/byte buffer (capacity doubles), so each character is copied a constant number of times on average: O(n), and only one final String is created."));
C.push(B("StringBuffer does the same as StringBuilder but every method is synchronized. With one thread it should be equal or slightly slower; its benefit appears only with shared access."));
C.push(B("Expected: for a short 10-field ticket the difference is small in absolute terms (but allocation is still about 2\u00d7); for long tickets the gap becomes dramatic."));
C.push(H2("2. Workloads designed"));
C.push(tbl(["Workload", "Fields per ticket", "Tickets", "Purpose"], [
  ["W1 short ticket", "10", "100,000", "Many small strings \u2013 overhead per ticket dominates"],
  ["W2 many big tickets", "50", "10,000", "Workload suggested in the question"],
  ["W3 one huge ticket (own)", "20,000", "1", "Exposes the O(n\u00b2) copying of repeated +"],
  ["Scaling (own)", "1,000 \u2026 32,000", "1", "Check growth rate when n doubles"],
], [3, 2, 2, 6]));
C.push(gap());
C.push(H2("3. Measured results (average of 5 runs)"));
const q1rows = {
  "W1 \u2013 10 fields \u00d7 100,000 tickets": [["String +", 35.266, 99.18, "1,000,000"], ["StringBuilder", 19.442, 48.83, "100,000"], ["StringBuffer", 9.740, 49.59, "100,000"], ["String.join", 12.753, 34.33, "100,000"], ["StringJoiner", 21.260, 26.74, "100,000"], ["String.format", 78.666, 305.82, "2,000,000"]],
  "W2 \u2013 50 fields \u00d7 10,000 tickets": [["String +", 17.447, 162.51, "500,000"], ["StringBuilder", 6.911, 29.14, "10,000"], ["StringBuffer", 5.857, 29.22, "10,000"], ["String.join", 3.542, 14.11, "10,000"], ["StringJoiner", 4.659, 11.22, "10,000"], ["String.format", 40.600, 159.74, "1,000,000"]],
  "W3 \u2013 one ticket with 20,000 fields": [["String +", 223.615, 2458.57, "20,000"], ["StringBuilder", 0.207, 0.81, "1"], ["StringBuffer", 0.224, 0.81, "1"], ["String.join", 0.195, 0.57, "1"], ["StringJoiner", 0.233, 0.50, "1"], ["String.format", 1.293, 6.00, "40,000"]],
};
for (const [name, rows] of Object.entries(q1rows)) {
  C.push(H3(name));
  C.push(tbl(["Approach", "Avg time (ms)", "Allocated (MB)", "Est. String objects"], rows.map((r) => [r[0], f3(r[1]), f2(r[2]), r[3]]), [3, 2, 2, 2]));
  C.push(gap());
}
C.push(P([bold("Estimated intermediate String objects: "), run("n per ticket for repeated +, 1 for the builders and join, about 2n for String.format (one formatted String plus one temporary per field). "),
  bold("Memory before/after: "), run("the heap-used difference measured after System.gc() was about 0 MB for every approach because the garbage is collected; therefore bytes allocated per thread is the more informative memory measure and is the one tabulated.")]));
C.push(H3("Scaling of one ticket with n fields"));
C.push(tbl(["n fields", "String + (ms)", "StringBuilder (ms)", "Chars copied by + (est.)", "Time ratio"], [
  ["1,000", "1.180", "1.2990", "5,500,000", "0.9 (JIT warm-up)"], ["2,000", "1.708", "0.0178", "22,000,000", "95.8"], ["4,000", "8.171", "0.1194", "88,000,000", "68.4"],
  ["8,000", "35.724", "0.1803", "352,000,000", "198.1"], ["16,000", "144.949", "0.4040", "1,408,000,000", "358.8"], ["32,000", "585.973", "0.4054", "5,632,000,000", "1445.5"]], [2, 2, 2, 3, 2]));
C.push(...img("q1.png", 450, 285, "Figure 1: Q1 \u2013 time for one ticket as n grows (log-log axes)."));
C.push(H2("4. Prediction versus observation"));
C.push(B("Confirmed: repeated + is quadratic. Each doubling of n multiplied the time by about 4 (8,000\u219216,000: 4.06\u00d7; 16,000\u219232,000: 4.04\u00d7). StringBuilder stayed below 0.5 ms for all sizes (the very small times are close to the timer noise)."));
C.push(B("Confirmed: allocation. For W3 the + approach allocated 2,458 MB to build a text of only about 0.27 MB; StringBuilder allocated 0.81 MB."));
C.push(B("Partly surprising: in W1 StringBuffer (9.7 ms) was consistently faster than StringBuilder (19.4 ms), although in W2 and W3 they are equal. Both classes share the same implementation code (AbstractStringBuilder), and the uncontended synchronisation is cheap or removed by the JIT, so StringBuffer cannot truly be 2\u00d7 faster. The most likely cause is JIT compilation order (StringBuilder was measured first, while the shared code was still being optimised). I did not re-run with reversed order, so this explanation is a hypothesis, not a proven fact."));
C.push(B("String.format was the slowest and allocated the most (about 3\u00d7 more than +) in W1, because it parses the format string for every call."));
C.push(H2("5. Design decision \u2013 multithreaded booking server"));
C.push(tbl(["Test (4 threads \u00d7 50,000 appends, expected length 800,000)", "Corrupted trials", "Threads crashed", "Shortest length"], [
  ["StringBuilder, no synchronisation", "1 / 5", "2", "400,000"], ["StringBuffer", "0 / 5", "0", "800,000"], ["StringBuilder inside synchronized block", "0 / 5", "0", "800,000"]], [5, 2, 2, 2]));
C.push(gap());
C.push(B([bold("Per-ticket text: StringBuilder. "), run("A ticket is built inside one method by one thread (a local variable), so nothing is shared and synchronisation would be pure overhead.")]));
C.push(B([bold("Shared booking log: StringBuffer "), run("(or a StringBuilder guarded by one lock, or better a thread-safe logger/queue). The unsynchronised StringBuilder lost data and threw exceptions in the experiment. Even on this single-core machine corruption appeared; on a multi-core server it will be more frequent. Note that StringBuffer makes each single append atomic, but not a sequence of appends that must stay together, so appending a whole log line with one call (or locking the whole sequence) is recommended.")]));
C.push(H2("6. Alternative concept and complexity analysis"));
C.push(P("String.join(), StringJoiner and String.format() instead of an explicit StringBuilder:"));
C.push(code(`String ticket = String.join(";", fields) + ";";                 // join: computes total length first, one copy
StringJoiner j = new StringJoiner(";", "", ";");                 // joiner: builds an internal buffer
for (String f : fields) j.add(f);  String t2 = j.toString();
String t3 = String.format("PNR:%s Name:%s Coach:%s Berth:%s Fare:%s", ...);   // fixed template, parsed on each call`));
C.push(tbl(["Approach", "Time for n fields", "Characters copied", "Extra space created"], [
  ["Repeated +", "O(n\u00b2)", "L\u00b7n\u00b2/2", "n intermediate Strings; total garbage about L\u00b7n\u00b2/2 characters (measured 2,458 MB for n=20,000)"],
  ["StringBuilder / StringBuffer", "O(n) amortised", "\u2264 about 2\u00b7L\u00b7n (doubling)", "One buffer (up to 2\u00d7 final size) + 1 final String"],
  ["String.join / StringJoiner", "O(n)", "about L\u00b7n", "join: exactly one result array; StringJoiner: one buffer (smallest allocation measured)"],
  ["String.format per field", "O(n) but large constant", "about L\u00b7n plus parsing", "A Formatter and temporary Strings per call (measured 3\u00d7 the allocation of +, in W1)"]], [3, 2, 2, 5]));
C.push(gap());
C.push(P([bold("Conclusion Q1. "), run("The choice matters once text is built in a loop and tickets are long; the difference is negligible only for a handful of fields. Use StringBuilder (or String.join when the fields are already in an array) for tickets and StringBuffer/locking for shared logs.")]));
C.push(shot("Task01 console output (StringBenchmark)"));
C.push(pb());

// ================= Q2 =================
C.push(H1("Q2 \u2013 When Is ArrayList an Appropriate Engineering Choice? (4 marks)"));
C.push(H2("1. Experiment design"));
C.push(P("A playlist of Song records was built with sizes 10,000, 50,000 and 100,000. For every operation a fresh structure was created outside the timing, then 2,000 operations (front/middle insertions and removals, random get(k)) were timed; \u201cadd at end\u201d adds n songs one by one; \u201citerate\u201d traverses all songs. ArrayDeque and a plain array with manual shifting were included for the second part of the question."));
C.push(P([bold("Prediction: "), run("ArrayList: O(1) amortised append and O(1) get(k), O(n) insert/remove at the front or middle (shifting). LinkedList: O(1) at both ends but O(n) to reach the middle or the k-th node (pointer walking, cache-unfriendly). Iteration is O(n) for both, with ArrayList faster in practice (contiguous memory).")]));
C.push(H2("2. Results at n = 100,000 (ms, average of 5 runs)"));
const ops = [["add at end (n adds)", "add at end (all n)"], ["insert at front", "insert at front (2,000)"], ["insert in middle", "insert in middle (2,000)"], ["remove from front", "remove from front (2,000)"], ["remove from middle", "remove from middle (2,000)"], ["get k-th (2000 gets)", "get k-th (2,000 gets)"], ["iterate all", "iterate all"]];
const g = (op, s) => (data.q2[op + "|" + s] && data.q2[op + "|" + s][100000] !== undefined) ? f3(data.q2[op + "|" + s][100000]) : "n/a";
C.push(tbl(["Operation", "ArrayList", "LinkedList", "ArrayDeque", "Plain array"], ops.map(([k, l]) => [l, g(k, "ArrayList"), g(k, "LinkedList"), g(k, "ArrayDeque"), g(k, "plain array")]), [4, 2, 2, 2, 2]));
C.push(gap());
C.push(H3("Growth with playlist size (ms for 2,000 operations)"));
const gr = (op, s) => [10000, 50000, 100000].map((n) => f2(q2(op, s, n)));
C.push(tbl(["Operation / structure", "10,000", "50,000", "100,000"], [
  ["Insert in middle \u2013 ArrayList", ...gr("insert in middle", "ArrayList")], ["Insert in middle \u2013 LinkedList", ...gr("insert in middle", "LinkedList")],
  ["get k-th \u2013 ArrayList", ...gr("get k-th (2000 gets)", "ArrayList")], ["get k-th \u2013 LinkedList", ...gr("get k-th (2000 gets)", "LinkedList")],
  ["Insert at front \u2013 ArrayList", ...gr("insert at front", "ArrayList")], ["Insert at front \u2013 LinkedList", ...gr("insert at front", "LinkedList")]], [5, 2, 2, 2]));
C.push(...img("q2.png", 600, 228, "Figure 2: Q2 \u2013 middle insertion and random access grow linearly for LinkedList."));
C.push(P([bold("Observations. "), run("LinkedList middle insertion grew from 30 ms to 146 ms (about 5\u00d7 for 10\u00d7 data, with noise at the smallest size) and get(k) from 7.4 ms to 78 ms, both clearly linear. ArrayList get(k) stayed near 0.1 ms. LinkedList was faster at the front (0.04 ms vs 15 ms for ArrayList at 100,000). Surprisingly, ArrayList middle insertion was fast (7.7 ms) because System.arraycopy moves memory in bulk, so an O(n) shift is much cheaper than an O(n) pointer walk. Some small-size figures (for example ArrayDeque front insertion at 50,000) are affected by JIT/GC noise on the one-core test machine.")]));
C.push(H2("3. When is ArrayList reasonable, and when to stop recommending it?"));
C.push(B("Reasonable: playlists are read, iterated and jumped into far more often than they are edited; edits happen mostly at the end or occasionally in the middle; sizes are in the thousands. get(k) and iteration are fastest, and memory per element is lowest (one array slot)."));
C.push(B("Stop recommending when most operations are insertions or removals at the front, or when many thousands of edits at the head of a very large list occur (cost grows as 2,000 \u00d7 n element moves; 15 ms per 2,000 front operations at n = 100,000 and growing linearly)."));
C.push(B("LinkedList is recommended only if operations happen through an iterator already positioned at the node; because get(k) is O(n), it is rarely the best choice for a playlist."));
C.push(H2("4. Changed condition \u2013 \u201cUp Next\u201d queue"));
C.push(tbl(["Queue size", "ArrayList (ms)", "LinkedList (ms)", "ArrayDeque (ms)"], [1000, 10000, 50000].map((n) => [String(n), f2(q2("UpNext mixed ops", "ArrayList", n)), f2(q2("UpNext mixed ops", "LinkedList", n)), f2(q2("UpNext mixed ops", "ArrayDeque", n))]), [2, 2, 2, 2]));
C.push(...img("q2b.png", 450, 270, "Figure 3: Q2 \u2013 100,000 mixed addFirst/addLast/removeFirst operations."));
C.push(P("The Up Next queue needs add and remove at both ends and never random access, which is exactly a deque. ArrayList took 50\u2013292 ms (every front operation shifts the whole array), while ArrayDeque took 0.2\u20130.4 ms, about 170\u2013780\u00d7 faster, and LinkedList was close (0.4\u20131 ms) but allocates a node per element. Recommendation: keep the playlist in an ArrayList and use ArrayDeque for the Up Next queue."));
C.push(H2("5. Alternative concept and complexity analysis"));
C.push(tbl(["Operation", "ArrayList", "LinkedList", "ArrayDeque", "Plain array + manual shifting"], [
  ["Add at end", "O(1) amortised", "O(1)", "O(1) amortised", "O(1) if spare capacity, else O(n) copy"],
  ["Insert at front", "O(n)", "O(1)", "O(1) amortised", "O(n)"], ["Insert in middle", "O(n)", "O(n) to find + O(1) link", "not supported", "O(n)"],
  ["get k-th", "O(1)", "O(n)", "not supported", "O(1)"], ["Iterate all", "O(n), fastest", "O(n), pointer chasing", "O(n)", "O(n)"],
  ["Space per element", "one reference slot (4\u20138 bytes) plus spare capacity (up to 50%)", "node object: item + prev + next + header (about 24\u201332 bytes)", "one array slot plus spare capacity", "one slot, fixed capacity"]], [3, 3, 3, 3, 4]));
C.push(gap());
C.push(P("A plain array with manual shifting (implemented in ListBenchmark.java) has the same complexity as ArrayList but uses an explicit Java loop to shift elements: front insertion took 72.9 ms and middle insertion 40.6 ms for 2,000 operations at n = 100,000, compared with 15.4 ms and 7.7 ms for ArrayList, because ArrayList uses the native System.arraycopy and also grows automatically."));
C.push(shot("Task02 console output (ListBenchmark)"));
C.push(pb());

// ================= Q3 =================
C.push(H1("Q3 \u2013 Engineer the hashCode\u2013equals Contract (6 marks)"));
C.push(H2("1. Designed tests with predictions and observations"));
C.push(tbl(["Test", "Scenario", "Prediction and reason", "Observed"], [
  ["1 (looks correct)", "HashSet<Student>: 3 different students, then the same object added again", "Size 3. The same object has the same identity hashCode, so it lands in the same bucket and equals() returns true.", "3 \u2013 correct (misleading!)"],
  ["2 (exposes duplicates)", "1,000 scans of the same student, a new object each time", "About 1,000. equals() says all are equal, but Object.hashCode() differs, so HashSet searches different buckets and never calls equals().", "1000; contains(new equal object) = false. Corrected class: size 1, contains = true"],
  ["3 (own) mutable key", "Correct hashCode on regNo, but regNo is changed after insertion", "Size stays 1, but the entry sits in the bucket of the old hash: contains(new value) = false and contains(old value) = false.", "size 1; contains = false in all cases; remove(m) = false; re-adding the student gives size 2"],
  ["4 (own) TreeSet", "Comparator uses only the first 5 characters of regNo (inconsistent with equals)", "Size 1: 192311001 and 192311002 compare as 0, so TreeSet treats them as duplicates though equals() says they differ.", "1 \u2013 correct"]], [2, 3, 5, 4]));
C.push(gap());
C.push(H2("2. Complete the Code (Attendance)"));
C.push(P([bold("Before completing TODO 1 the program printed: "), run("Unique students: 3", { bold: true, color: "C00000" }), run("  (wrong).  "), bold("After completing TODO 1 it printed: "), run("Unique students: 2", { bold: true, color: "007A33" }), run("  (matches the expected output).")]));
C.push(code(rd("Task03/after/Attendance.java")));
C.push(gap());
C.push(shot("Task03/before output (3) and Task03/after output (2)"));
C.push(H2("3. Inquiry"));
C.push(B([bold("Why duplicates appear. "), run("Without an override, hashCode() is the identity hash, so two equal students almost always get different hash codes. HashSet first uses the hash to choose a bucket and compares hashes before calling equals(); equals() is therefore never reached for objects in different buckets.")]));
C.push(B([bold("How HashSet locates an element. "), run("(1) compute hashCode(), spread it and take index = hash & (table length \u2212 1); (2) in that bucket, compare the stored hash and then equals() with each entry (a linked list or tree); (3) the element exists only if both match. The contract: equal objects must have equal hash codes (the reverse is not required).")]));
C.push(B([bold("Changing a hashed field after insertion. "), run("The entry stays in the bucket calculated from the old value. Lookups with the new value search another bucket; lookups with the old value find the entry but equals() now fails. The element becomes unreachable (a \u201cghost\u201d), cannot be removed, and the same student can be added again (Test 3). Fix: make hashed fields final/immutable, or remove and re-insert around any change.")]));
C.push(B([bold("Correction and verification. "), run("Add `@Override public int hashCode() { return regNo.hashCode(); }` using exactly the same field(s) as equals(), and make regNo final. Verify by running the Attendance program (size 2), by Test 2 (size 1 instead of 1000) and by the check that for any two objects with equals() true the hash codes are equal.")]));
C.push(H2("4. Alternative concept and complexity analysis"));
C.push(P("Alternatives: TreeSet<Student> with a Comparator on regNo, or HashMap<String, Student> keyed by regNo (putIfAbsent keeps the first scan). Measured time to process n scans (each unique id scanned 3 times), average of 5 runs:"));
C.push(tbl(["Scans", "HashSet (ms)", "TreeSet (ms)", "HashMap (ms)"], [["30,000", "1.48", "23.79", "0.99"], ["300,000", "27.63", "101.40", "42.77"], ["1,200,000", "159.47", "670.33", "203.60"]], [2, 2, 2, 2]));
C.push(gap());
C.push(tbl(["Structure", "Time per scan", "Space for n unique students", "Notes"], [
  ["HashSet<Student>", "O(1) average, O(n) worst (O(log n) with treeified buckets)", "O(n): backing HashMap, one entry node (about 32 bytes) per student plus the table", "Needs a correct hashCode/equals pair"],
  ["HashMap<String,Student>", "O(1) average", "O(n): same as HashSet plus the key reference", "Needs only String hashing, which is already correct; the safest design"],
  ["TreeSet<Student> (Comparator on regNo)", "O(log n) comparisons, always", "O(n): one tree node (about 40 bytes: key, left, right, parent, colour)", "No hashCode needed; sorted output; the comparator must be consistent with equals"]], [3, 3, 4, 4]));
C.push(gap());
C.push(P([bold("Conclusion Q3. "), run("equals() and hashCode() are one contract. HashSet appeared to work in Test 1 only by accident. Keying a HashMap by the immutable regNo String avoids the whole class of bugs.")]));
C.push(pb());

// ================= Q4 =================
C.push(H1("Q4 \u2013 Choosing a Map for a Pharmacy Inventory (6 marks)"));
C.push(H2("1. Design reasoning before implementation"));
C.push(tbl(["Map", "put", "get", "Iteration", "Natural report order", "Extra work for the other report"], [
  ["HashMap", "O(1) avg", "O(1) avg", "O(capacity + n), unordered", "none", "Code order: copy keys + sort, O(n log n). Insertion order: needs a parallel list."],
  ["TreeMap", "O(log n)", "O(log n)", "O(n), sorted by key", "CODE order", "Insertion order: needs a parallel list."],
  ["LinkedHashMap", "O(1) avg", "O(1) avg", "O(n), insertion order", "INSERTION order", "Code order: copy keys + sort, O(n log n)."]], [2, 2, 2, 3, 3, 6]));
C.push(gap());
C.push(H2("2. Measured results (average of 5 runs; 100,000 lookups of random existing codes)"));
const m4 = (met, s, n) => data.q4[met + "|" + s][n];
for (const n of [10000, 100000, 500000]) {
  C.push(H3("n = " + n.toLocaleString("en-US") + " medicines"));
  const rows = ["HashMap", "TreeMap", "LinkedHashMap", "Hashtable"].map((s) => [s, f2(m4("build_ms", s, n)), f2(m4("lookups_100k_ms", s, n)), f1(m4("ns_per_lookup", s, n)), f2(m4("code_report_ms", s, n)), f2(m4("insertion_report_ms", s, n))]);
  rows.push(["Sorted array + binarySearch", f2(m4("build_ms", "sorted array", n)), f2(m4("lookups_100k_ms", "sorted array", n)), f1(m4("ns_per_lookup", "sorted array", n)), f2(m4("code_report_ms", "sorted array", n)), "n/a"]);
  C.push(tbl(["Structure", "Build (ms)", "100k lookups (ms)", "ns per lookup", "Code-order report (ms)", "Insertion-order report (ms)"], rows, [3, 2, 2, 2, 3, 3]));
  C.push(gap());
}
C.push(...img("q4.png", 600, 228, "Figure 4: Q4 \u2013 lookup time and code-order report time versus number of medicines."));
C.push(P([bold("Order check. "), run("In all runs the reports matched the requirement (code ok = true, insertion ok = true) only when the natural map was used or the extra work was done. Iterating a HashMap directly gave neither order (raw code-order = false, insertion-order = false); TreeMap raw iteration gave code order only; LinkedHashMap raw iteration gave insertion order only.")]));
C.push(P([bold("Cost of choosing the \u201cwrong\u201d map. "), run("For 500,000 medicines the code-order report took 56 ms on TreeMap but 213 ms on HashMap and 422 ms on LinkedHashMap (copy and sort). The insertion-order report took 80 ms on LinkedHashMap but 821 ms on TreeMap because n tree lookups are needed. (Absolute figures contain some GC noise; the ordering of costs is stable.)")]));
C.push(P([bold("Memory (100,000 medicines, approximate, one measurement after System.gc()). "), run("HashMap 11.67 MB, TreeMap 11.44 MB, LinkedHashMap 12.44 MB, Hashtable 11.43 MB. These figures include the shared Medicine and name-String objects, so only the differences matter: the maps differ by less than 1 MB.")]));
C.push(H2("3. Scalability inquiry"));
C.push(B([bold("Acceptable response time (my definition): "), run("one lookup must complete in under 1 ms (1,000,000 ns) so that hundreds of lookups per day, or a screen that performs 100 lookups at once, remain far below the 100 ms limit at which users perceive delay.")]));
C.push(B("All structures meet this criterion by a wide margin: even the slowest, TreeMap at 500,000 medicines, took about 1.5 \u00b5s per lookup. HashMap took 40\u2013110 ns."));
C.push(B([bold("Decision: "), run("deploy HashMap (or LinkedHashMap if the insertion-order report is needed often) for lookups, and produce the evening code-order report by sorting the keys (about 0.2 s for 500,000 items, once a day), or use TreeMap if the sorted report is requested often. The remaining cost of a lookup comes from String hashing/comparison and cache misses (pointer-chasing across scattered objects); for TreeMap each lookup does about log2(n) \u2248 19 string comparisons at 500,000 items.")]));
C.push(B([bold("Hashtable is not preferred "), run("because every method is synchronized (coarse lock for all threads), it forbids null keys and values, it has no ordering, and it is a legacy class; ConcurrentHashMap is the modern thread-safe choice and HashMap the single-thread choice. In the measurements Hashtable gave no useful advantage over HashMap.")]));
C.push(H2("4. Alternative: sorted array with Arrays.binarySearch"));
C.push(tbl(["Operation", "Sorted array + binarySearch", "HashMap", "TreeMap"], [
  ["Lookup", "O(log n); measured 197\u20131,835 ns", "O(1) average; 40\u2013110 ns", "O(log n); 227\u20131,542 ns"],
  ["Insert one medicine", "O(n): binarySearch then System.arraycopy shift (1,000 inserts into 500,000: 0.52 ms)", "O(1) average (0.25 ms per 1,000)", "O(log n) (0.20 ms per 1,000)"],
  ["Code-order report", "O(n), already sorted (6.4 ms at 500,000)", "O(n log n) sort", "O(n), (56 ms)"],
  ["Insertion-order report", "not possible without an extra list", "extra list needed", "extra list needed"],
  ["Space", "one reference per medicine (smallest); needs spare capacity", "table + entry node per item", "tree node per item"]], [3, 5, 4, 4]));
C.push(gap());
C.push(P([bold("Conclusion Q4. "), run("Choose by the access pattern: HashMap for the many lookups, a sorted view (TreeMap or sort-at-report time) for the code-order report, LinkedHashMap for insertion order. Q7 builds on this question.")]));
C.push(shot("Task04 console output (MapBenchmark)"));
C.push(pb());

// ================= Q5 =================
C.push(H1("Q5 \u2013 Derive a Generic Ranking Component from a Principle (7 marks)"));
C.push(H2("1. Design derived from the principle"));
C.push(P("The principle says a ranking is correct only if the comparison is consistent: antisymmetric (sign of compare(a,b) is the opposite of compare(b,a)), transitive, and equal items compare as 0. From this I derived:"));
C.push(B([bold("Comparator, not Comparable. "), run("A team has no single \u201cnatural\u201d order: a points table is only one way to rank it (by name, by goal difference or by a different sport\u2019s rules would also be valid). Comparable<Team> would fix one order in the class itself, and the same Leaderboard<T> could not reuse it for players or quiz teams with different rules. A Comparator is supplied from outside, so the component stays generic. Comparable remains useful for a type with one obvious order.")]));
C.push(B([bold("Multi-field comparison as a chain: "), run("Comparator.comparingInt(Team::points).reversed().thenComparing(comparingInt(Team::goalDiff).reversed()).thenComparing(Team::name). thenComparing is only consulted when the previous keys are equal, which makes the combined order lexicographic and therefore transitive if each key comparison is. Important: reversed() must be applied to the comparator of ONE key, not to the whole chain after the other keys have been added, otherwise the name order would also be reversed.")]));
C.push(B([bold("Generic class. "), run("Leaderboard<T> keeps a List<T> and a Comparator<? super T>. The wildcard `? super T` lets a Comparator<Object> or a comparator of a parent type rank children. The bounded type is used where the type itself must be comparable: `static <T extends Comparable<? super T>> List<T> rankNatural(Collection<? extends T> items)`.")]));
C.push(B([bold("Where wildcards are useful. "), run("Collection<? extends T> in addAll accepts a List<Student> into a Leaderboard<Person> (producer: read-only use); Comparator<? super T> in the constructor accepts general comparators (consumer). This is the PECS rule: producer extends, consumer super.")]));
C.push(H2("2. Complete the Code (LeaderboardDemo)"));
C.push(code(rd("Task05/LeaderboardDemo.java")));
C.push(gap());
C.push(P([bold("Output: "), run("[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]", { bold: true, color: "007A33" }), run("  (matches the expected output).")]));
C.push(P("Why Bisons is ranked above Panthers: both have 9 points and goal difference 4, so the first two keys compare as equal. The third key, the name, decides: \"Bisons\" is alphabetically before \"Panthers\". TODO 1 sorts a copy, so the internal list keeps its insertion order."));
C.push(shot("Task05 LeaderboardDemo output"));
C.push(H2("3. Test data"));
C.push(P([bold("Data set 1 (12 teams, several ties at each level). "), run("Result of the comparator chain: Eagles(9,6), Zebras(9,6) [tie on points and goal difference, decided by name]; Bisons(9,4), Panthers(9,4); Aces(6,3); Archers, Cobras, Falcons (6,1) in name order; Bulls(3,0); Comets, Dragons (3,\u22122); Wolves(0,\u22129). The ranking was identical for the original and the reversed input order, which shows it does not depend on arrival order.")]));
C.push(P([bold("Data set 2 (designed to break faulty comparators): "), run("teams with MAX_VALUE points, \u22122,000,000,000 points, 5 points and three teams tied at (7,3).")]));
C.push(tbl(["Comparator", "Antisymmetry violations", "Transitivity violations", "Finding"], [
  ["Correct chain BY_RANK", "0", "0", "Consistent; forward and reversed input give the identical order (Huge, TieA, TieB, TieC, Normal, Penalised)"],
  ["Faulty: b.points \u2212 a.points", "0", "12", "Integer overflow: compare(Huge, Penalised) = +147,483,649 although Huge has more points \u2013 the ranking is wrong"],
  ["Faulty: ignores the name tie-breaker", "0", "0", "Transitive but incomplete: TieA/TieB/TieC compare as equal, so the order depends on the input (forward: TieA TieB TieC; reversed: TieC TieB TieA)"]], [3, 2, 2, 7]));
C.push(gap());
C.push(P("On 2,000 random teams the subtraction comparator did not even throw an exception, but the first team in the result had \u22121,999,999,991 points, i.e. silently wrong. (TimSort only sometimes detects an inconsistent comparator, so a clean run is not proof of correctness.)"));
C.push(H2("4. Correctness inquiry \u2013 why the comparator satisfies the principle"));
C.push(B("Each key comparison (Integer.compare on points, Integer.compare on goal difference, String.compareTo on name) is itself a total preorder: antisymmetric and transitive. Integer.compare never overflows because it only compares, it does not subtract."));
C.push(B("reversed() on one key negates a total preorder, which keeps it a total preorder."));
C.push(B("thenComparing builds the lexicographic order: a < b if the first key says so, or if the first keys are equal and the next key says so. For a<b and b<c: let k be the first key where a and b differ and j the first where b and c differ; the earliest of k and j decides a versus c in the same direction, so a<c (transitivity). Equal items (all three keys equal) compare as 0 in every key and therefore as 0 overall."));
C.push(B("The names are unique, so the last key breaks every remaining tie and the ranking is deterministic. The brute-force checker in RankingDesign.java confirmed zero violations over all pairs and triples of the test sets, which supports (but is not the basis of) the argument above."));
C.push(H2("5. Alternative concept and complexity analysis"));
C.push(P("Alternatives: (a) Team implements Comparable<Team> with the same three-step compareTo (implemented as RankedTeam; it gave exactly the same order), or (b) three stable sorts, by name first, then goal difference, then points (the last sort has the highest priority)."));
C.push(tbl(["n", "Chain comparisons", "Three-sort comparisons", "Chain time (ms)", "Three-sort time (ms)", "Same result"], [
  ["10,000", "120,422", "290,250", "17.9", "21.7", "true"], ["100,000", "1,534,761", "3,242,213", "64.9", "90.5", "true"], ["1,000,000", "18,640,516", "35,701,382", "1627.7", "2120.2", "true"]], [2, 3, 3, 2, 3, 2]));
C.push(gap());
C.push(tbl(["Design", "Sorts", "Time", "Extra space", "Reuse for players/colleges"], [
  ["Comparator chain", "1", "O(n log n) comparisons, each up to 3 key comparisons", "O(n) copy (TimSort temporary array up to n/2)", "Best: write a new comparator, no change to the class"],
  ["Comparable<Team>", "1", "O(n log n)", "same", "Poor: the order is fixed inside Team, and every new type needs its own compareTo; only one order is possible"],
  ["Three stable sorts", "3", "3 \u00d7 O(n log n) (measured about 2\u00d7 the comparisons and 1.3\u20131.4\u00d7 the time)", "O(n) temporary space for each sort", "Easy to write but easy to get the order wrong, and it needs stable sorting and repeated passes"]], [3, 1, 4, 3, 4]));
C.push(gap());
C.push(P([bold("Conclusion Q5. "), run("The comparator chain is correct, reusable and the cheapest of the three designs. Comparable is acceptable only when the type has one obvious order.")]));
C.push(pb());

// ================= Q6 =================
C.push(H1("Q6 \u2013 Priority, Order and Fairness in a Queue (8 marks)"));
C.push(H2("1. Design"));
C.push(B("A plain Queue (LinkedList/ArrayDeque) is first-in-first-out: it serves in arrival order and cannot let a critical patient who arrives last overtake a minor case. Sorting after every arrival would cost O(n log n) per patient."));
C.push(B("A PriorityQueue is a binary heap ordered by a Comparator: the head is always the minimum element. Comparator: severity ascending (1 is critical), then arrival number ascending."));
C.push(B("Arrival order among equal severities is preserved by the arrival counter: PriorityQueue is not stable (equal elements may come out in any order), so a strictly increasing counter is stored in each Patient and used as the tie-breaker, making every pair of patients comparable and unequal."));
C.push(B("poll() removes and returns the next patient (O(log n)); to display the waiting list in treatment order, copy the queue and poll the copy, or sort a copy. Printing the PriorityQueue directly shows the internal heap array."));
C.push(H2("2. Complete the Code (Triage)"));
C.push(code(rd("Task06/Triage.java")));
C.push(gap());
C.push(P([bold("Output: "), run("Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)", { bold: true, color: "007A33" }), run("  (matches the expected output).")]));
C.push(P("Meena (arrival 1) and Kumar (arrival 4) both have severity 1; the counter compares 1 < 4, so Meena is served first. Likewise Ravi (arrival 0) is before John (arrival 2) at severity 3. The counter is increased by admit() and never reused, so the order among equal severities is the order of admission."));
C.push(shot("Task06 Triage output"));
C.push(H2("3. Correctness check with 8 patients (worked out by hand)"));
C.push(tbl(["Arrival no.", "0", "1", "2", "3", "4", "5", "6", "7"], [["Patient (severity)", "Ravi (3)", "Meena (1)", "John (3)", "Asha (5)", "Kumar (1)", "Lata (2)", "Divya (2)", "Tom (4)"]], [2, 1, 1, 1, 1, 1, 1, 1, 1]));
C.push(gap());
C.push(P("Hand-worked order: severity 1: Meena (1), Kumar (4); severity 2: Lata (5), Divya (6); severity 3: Ravi (0), John (2); severity 4: Tom; severity 5: Asha."));
C.push(code(`Hand-worked : Meena(S1) Kumar(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Tom(S4) Asha(S5)
poll() loop : Meena(S1) Kumar(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Tom(S4) Asha(S5)   -> identical
println(pq) : [Meena, Kumar, Lata, Tom, Ravi, John, Divya, Asha]   (heap array order)
Plain FIFO  : Ravi Meena John Asha Kumar Lata Divya Tom`));
C.push(P("Printing the PriorityQueue (toString/iterator) walks the heap array: it guarantees only that every parent is not larger than its children, not a complete order. Only the head (index 0) is guaranteed to be the minimum; the rest is in heap order (here Tom(S4) is printed before Ravi(S3))."));
C.push(H2("4. Growth investigation"));
C.push(P([bold("Prediction: "), run("add = O(log n) (sift-up) and poll = O(log n) (sift-down), so the cost per operation should grow only slowly (about +1 level, i.e. +3\u201310%, per doubling of n); poll costs more than add because sift-down does two comparisons per level, and an average add finishes in O(1) comparisons for random keys.")]));
C.push(tbl(["n", "Add all (ms)", "Poll all (ms)", "ns per add", "ns per poll"], [["10,000", "2.32", "5.23", "232.0", "522.5"], ["100,000", "4.75", "20.22", "47.5", "202.2"], ["1,000,000", "24.33", "463.98", "24.3", "464.0"]], [2, 2, 2, 2, 2]));
C.push(gap());
C.push(...img("q6.png", 600, 216, "Figure 5: Q6 \u2013 PriorityQueue cost per operation and comparison with alternative designs."));
C.push(P("Observation: the prediction is only partly confirmed. Poll is more expensive than add, as predicted, and the total work is far from quadratic: poll rose from 202 ns to 464 ns for a 10\u00d7 larger queue, in line with the extra heap levels plus cache misses once the heap no longer fits in the CPU cache. The 10,000-item figures (232 ns per add) are inflated by JIT warm-up and are not reliable; the per-add cost fell with n, which cannot be a real trend. Conclusion: each operation is well below 1 \u00b5s for up to 1 million waiting patients."));
C.push(H2("5. Starvation and a fair rule"));
C.push(P("With strict priority a severity-5 patient is served only when no patient of severity 1\u20134 is waiting. If the arrival rate of severe cases is at least the service rate, there is always someone more severe in the queue, so the minor patient waits indefinitely."));
C.push(P([bold("Fair rule (aging). "), run("Instead of the severity, order by an effective key = arrival time + W \u00d7 (severity \u2212 1), with W = 50 ticks (for example 50 minutes). A patient of severity s then behaves as if he had arrived (s \u2212 1)\u00d7W later than he really did, so a severity-5 patient is overtaken only by severe patients who arrive within 4W of him, after that his key is the smallest. The key is fixed at admission, so the heap stays valid and the cost remains O(log n); the alternative of raising severity values periodically would require updating queued elements (remove and re-add, O(log n) each).")]));
C.push(tbl(["Simulation (20,000 ticks; 1.2 severe arrivals per tick; 1 patient served per tick)", "Severity-5 patient", "Longest wait of a severity-1 patient"], [
  ["Strict priority", "NEVER served within 20,000 ticks", "2 ticks"], ["Aging, W = 50", "Served at tick 214", "3,307 ticks"]], [6, 4, 4]));
C.push(gap());
C.push(P([bold("Trade-off (important): "), run("in this overloaded simulation aging fixes starvation but critical patients can wait much longer, because the queue is 4,000 patients long and the key now partly follows arrival order. In a real department the rule must be tuned: a larger W protects critical cases, a smaller W protects minor ones; a hybrid is to keep strict priority for severity 1 and age only levels 2\u20135, or to use a hard waiting-time limit.")]));
C.push(H2("6. Deployment decision \u2013 5,000 patients per day"));
C.push(P("5,000 patients per day is 0.06 arrivals per second. Admitting and serving all 5,000 patients took 9.9 ms in total in the test, and one million operations cost under 0.5 ms each, so the data structure is not a bottleneck: capacity is thousands of times greater than required. Yes, the design can serve the city hospital. The real risks are fairness (aging) and clinical safety, not speed. It needs persistence/failover in a real system, which is outside this experiment."));
C.push(H2("7. Alternative concept and complexity analysis"));
C.push(tbl(["n", "PriorityQueue (ms)", "Sorted LinkedList (ms)", "5 FIFO queues (ms)"], [["5,000", "0.71", "41.57", "0.29"], ["20,000", "3.70", "1,086.54", "0.27"], ["50,000", "12.21", "18,566.48", "0.98"]], [2, 3, 3, 3]));
C.push(gap());
C.push(tbl(["Design", "Admit", "Serve next", "Space", "Comment"], [
  ["PriorityQueue (binary heap)", "O(log n)", "O(log n)", "O(n) array", "General: works for any number of levels or a complex comparator"],
  ["LinkedList kept sorted", "O(n) (walk to the position)", "O(1) (remove the head)", "O(n) nodes (about 24 bytes each)", "Measured: 18.6 s for 50,000 patients; grew 17\u00d7 for 2.5\u00d7 more patients (20,000 \u2192 50,000), clearly worse than linear (quadratic in theory; cache misses add to it)"],
  ["Array of 5 FIFO queues", "O(1)", "O(1) (check at most 5 queues)", "O(n + 5)", "Fastest and simplest for a fixed small number of levels; cannot express aging without moving patients between queues"]], [3, 2, 3, 3, 5]));
C.push(gap());
C.push(P([bold("Conclusion Q6. "), run("For five fixed severity levels the array of five FIFO queues is the fastest solution; the PriorityQueue is the general, flexible solution and is more than fast enough. Strict priority needs a fairness rule to avoid starvation.")]));
C.push(pb());

// ================= Q7 =================
C.push(H1("Q7 \u2013 Diagnose, Redesign and Validate (13 marks)"));
C.push(P([bold("Selected solution: "), run("Q4 \u2013 the pharmacy inventory (HashMap for lookups, evening reports by medicine code and by insertion order).")]));
C.push(H2("A. Diagnose the bottleneck (2 marks)"));
C.push(P("Original solution: HashMap<String, Medicine> for lookups, an insertion index for the insertion-order report, and for the code-order report: copy all keys, sort them, then look each one up. The pharmacy stock is queried and changed all day, and the report is produced repeatedly (my model: 100 reports, each after 20 new medicines, 1,000 stock updates and 200 lookups)."));
C.push(tbl(["Measurement (Original, n = 50,000)", "Value"], [
  ["Time of the sort in one code-order report", "19.8 ms (74% of the report)"], ["Copying the keys + 50,000 map look-ups", "7.1 ms"],
  ["String comparisons per report", "333,441 (n log\u2082 n = 780,482 is the upper bound; the HashMap order is partly sorted already, which helps TimSort)"],
  ["Keys that changed since the last report", "20 of 50,000 (0.04%)"],
  ["Allocation of the whole workload (100 reports)", "67 MB (n = 50,000), 131 MB (n = 100,000) \u2013 a new key list and sort buffer per report"]], [6, 6]));
C.push(gap());
C.push(B([bold("Where the time goes: "), run("sorting the keys, which grows as n log n and is done again for every report.")]));
C.push(B([bold("Unnecessary repeated work: "), run("yes. Between two reports only 20 keys are new, but all 50,000 are re-sorted, and the key list is re-created. Stock updates do not change the key set at all but still force the next report to start from scratch.")]));
C.push(B([bold("Responsible characteristic: "), run("HashMap has no order and no memory of an earlier order. It is excellent for lookup (O(1)) but any ordered view has to be rebuilt from nothing.")]));
C.push(H2("B. Design objective (2 marks)"));
C.push(B("Avoid re-sorting the whole collection when only a few keys were added: reduce string comparisons per report from about n log n to about n + k log k."));
C.push(B("Measurable targets: at least 5\u00d7 fewer comparisons per report (n = 50,000, k = 20), at least 3\u00d7 lower total time of the 100-report workload, and less memory allocated."));
C.push(B("Keep lookups O(1) (no slower than HashMap) and keep reports correct (identical output to the original, including order)."));
C.push(H2("C. Design of the revised solution (4 marks)"));
C.push(P("The redesigned class Inventory keeps three structures:"));
C.push(code(`class Inventory
  HashMap<String,Med>  map          // O(1) lookup and stock updates (unchanged)
  ArrayList<Med>       insertion    // insertion-order report, no look-ups needed
  Med[]                sorted       // cached array, always sorted by code
  ArrayList<Med>       pending      // medicines added since the last code report

add(code):       m = new Med(code); if map.put(code,m)==null: insertion.add(m); pending.add(m)
updateStock(c):  map.get(c).stock += x            // Med is mutable; 'sorted' holds references, so it stays valid
reportByCode():  if pending not empty:
                     sort(pending)                 // k log k
                     sorted = merge(sorted, pending)   // n + k, like merge sort's last step
                     pending.clear()
                 for m in sorted: print(m)         // O(n), no sorting, no look-ups`));
C.push(gap());
C.push(B([bold("What changed: "), run("the sorted order is kept between reports and only the new medicines are inserted into it, by merging two sorted arrays.")]));
C.push(B([bold("Property exploited: "), run("two sorted sequences can be merged in linear time, and the key set changes slowly (a few new codes per report, while stock changes never alter the key set). Storing references to mutable Med objects makes stock updates invisible to the sorted array.")]));
C.push(B([bold("Operations avoided: "), run("the full O(n log n) sort, the n hash look-ups of the report (the sorted array holds the objects) and the copy of the key set.")]));
C.push(B([bold("Extra memory/preprocessing: "), run("one more array of n references (about 4\u20138 bytes per medicine) plus a temporary array of n + k during the merge; the first report still costs a full sort (of pending).")]));
C.push(B([bold("New trade-offs: "), run("more code and more state to keep consistent (three structures); the merge still copies n references per report that has new medicines, so it is O(n) not O(k); the code assumes medicines are never deleted (deletions need a removal from the sorted array, O(n)); a large k loses the advantage (see the evaluation).")]));
C.push(P([bold("Reference baseline (not my redesign): "), run("TreeMap keeps the keys sorted automatically at O(log n) per insertion. It is the \u201cjust replace the collection\u201d answer, so it is measured only for comparison and for an honest defence.")]));
C.push(H2("D. Prediction before implementing (1 mark)"));
C.push(B("Comparisons per code report fall from about 335,000 to about n + k = 50,020 plus the sort of 20 items, i.e. roughly 6\u20137\u00d7 fewer."));
C.push(B("Total workload time falls by about 3\u201310\u00d7 for small k because the sort (about three quarters of the report) disappears. Allocation falls because the key list and sort buffer are no longer created for every report."));
C.push(B("The improvement shrinks as k approaches n, because the pending sort then becomes a full sort. Lookup time is unchanged (the same HashMap)."));
C.push(H2("E. Implementation and validation (2 marks)"));
C.push(P("Same test conditions for all designs (PharmacyRedesign.java): 100 cycles, 1,000 stock updates per cycle, average of 3 runs after a warm-up, identical random operations, and an order-sensitive checksum of all reports to prove the outputs were identical (no OUTPUT MISMATCH was reported)."));
C.push(H3("Comparisons per code report (n = 50,000 existing medicines)"));
C.push(tbl(["New medicines k", "Original", "Redesign", "Ratio", "Same output"], [["20", "335,139", "47,540", "7.0\u00d7", "true"], ["500", "336,927", "50,900", "6.6\u00d7", "true"], ["5,000", "364,568", "59,990", "6.1\u00d7", "true"], ["50,000", "558,451", "149,998", "3.7\u00d7", "true"]], [3, 3, 3, 2, 2]));
C.push(gap());
C.push(H3("Total time of the 100-cycle workload, ms (MB allocated)"));
C.push(tbl(["n", "k new per cycle", "Lookups per cycle", "Original", "TreeMap", "Redesign", "Speed-up vs original"], [
  ["20,000", "20", "200", "377.6 (29)", "177.0 (3)", "147.7 (12)", "2.6\u00d7"], ["50,000", "20", "200", "972.7 (67)", "264.5 (5)", "134.1 (27)", "7.3\u00d7"],
  ["100,000", "20", "200", "1983.4 (131)", "828.1 (9)", "213.6 (51)", "9.3\u00d7"], ["50,000", "20", "100,000", "1941.1 (67)", "5262.9 (5)", "1090.9 (27)", "1.8\u00d7"],
  ["50,000", "500", "200", "1551.0 (133)", "432.5 (40)", "304.1 (71)", "5.1\u00d7"], ["50,000", "5,000", "200", "7464.9 (756)", "951.8 (390)", "1042.9 (512)", "7.2\u00d7"]], [2, 2, 2, 3, 3, 3, 3]));
C.push(gap());
C.push(...img("q7.png", 600, 253, "Figure 6: Q7 \u2013 total workload time for the original, TreeMap and redesigned solutions (log scale)."));
C.push(H2("F. Defend the final design (2 marks)"));
C.push(B([bold("Prediction supported? "), run("Yes for the comparison count (7.0\u00d7 against the predicted 6\u20137\u00d7) and for the time (2.6\u20139.3\u00d7 for small k, within the predicted 3\u201310\u00d7 except at n = 20,000, where fixed costs dominate). Allocation fell by 2.4\u20132.6\u00d7 for small k. The objective of at least 3\u00d7 lower time was reached for n \u2265 50,000 with realistic lookup counts.")]));
C.push(B([bold("Magnitude: "), run("974 ms \u2192 134 ms at n = 50,000 (7.3\u00d7) and 1,983 ms \u2192 214 ms at n = 100,000 (9.3\u00d7). The advantage grows with n, as expected from n log n versus n.")]));
C.push(B([bold("Where the prediction was too optimistic: "), run("the comparisons fall 7\u00d7 but not 15\u00d7, because the original sort already benefits from the partly sorted HashMap iteration order. When lookups dominate (100,000 per cycle) the whole-workload speed-up is only 1.8\u00d7 because lookup time is the same in both solutions. At k = 50,000 per report, comparisons only drop by 3.7\u00d7.")]));
C.push(B([bold("New costs/trade-offs: "), run("an extra sorted array plus temporary merge arrays (still only 51 MB allocated against 131 MB for the original at n = 100,000); more complex code; deletions are not supported; the O(n) copy remains per report.")]));
C.push(B([bold("Is the TreeMap still better in some situations? "), run("Yes. For 200 lookups per cycle TreeMap (828 ms at n = 100,000) was only about 4\u00d7 slower than the redesign and has much simpler code; at k = 5,000 new medicines per cycle TreeMap (952 ms) was slightly faster than the redesign (1,043 ms). When lookups are frequent (100,000 per cycle) TreeMap was 4.8\u00d7 slower than the redesign (5,263 ms vs 1,091 ms) because of its O(log n) string comparisons per lookup. The redesign is therefore justified when lookups dominate and the key set changes slowly; for a pharmacy with only hundreds of lookups per day, TreeMap is an equally defensible and simpler choice.")]));
C.push(B([bold("When the original is preferable: "), run("if the code-order report is produced once per day (a single sort of 500,000 items took about 0.2 s), if code simplicity matters more than speed, or if most keys change between reports.")]));
C.push(P([bold("Final engineering conclusion: "), run("the repeated full sort was the diagnosed waste, and exploiting the slowly changing key set with a cached sorted array plus a linear merge removed it, achieving the stated objectives. The result depends on the workload mix (k and the number of lookups), so the design should be adopted only after measuring the real usage pattern.")]));
C.push(shot("Task07 console output (PharmacyRedesign)"));
C.push(pb());

// ================= Summary & Appendix =================
C.push(H1("Summary of Decisions"));
C.push(tbl(["Task", "Decision", "Main evidence"], [
  ["Q1", "StringBuilder per ticket; StringBuffer (or lock) for the shared log", "+ is O(n\u00b2): 224 ms vs 0.2 ms for 20,000 fields; unsynchronised StringBuilder corrupted the log"],
  ["Q2", "ArrayList for the playlist; ArrayDeque for \u201cUp Next\u201d", "LinkedList get(k) 78 ms vs 0.09 ms; Up Next 0.4 ms (ArrayDeque) vs 290 ms (ArrayList)"],
  ["Q3", "Override hashCode with the same fields as equals; make the field final", "Size 3 before and 2 after; 1000 duplicates of one student"],
  ["Q4", "HashMap/LinkedHashMap for lookups, sorted view for code order", "40\u2013110 ns vs 230\u20131,540 ns per lookup; TreeMap has code order for free"],
  ["Q5", "Comparator chain with Leaderboard<T>", "0 contract violations; subtraction comparator: 12 transitivity violations"],
  ["Q6", "PriorityQueue with severity + arrival counter, with aging", "Hand check matches; strict priority starved the S5 patient; 5,000 patients in 9.9 ms"],
  ["Q7", "Cached sorted array + merge for the pharmacy report", "7\u00d7 fewer comparisons; 2.6\u20139.3\u00d7 faster; limits shown honestly"]], [1, 5, 7]));
C.push(gap());
C.push(H2("Repository structure and how to run"));
C.push(code(`CSA09-<RegisterNumber>-Assignment2/
  README.md
  Task01/StringBenchmark.java        (+ q1_output.txt, q1_results.csv, screenshots/)
  Task02/ListBenchmark.java
  Task03/before/Attendance.java      Task03/after/Attendance.java      Task03/HashContractTests.java
  Task04/MapBenchmark.java
  Task05/LeaderboardDemo.java        Task05/RankingDesign.java
  Task06/Triage.java                 Task06/TriageStudy.java
  Task07/PharmacyRedesign.java

Compile and run inside a task folder:   javac File.java   then   java File
(Task04 and Task07 use more memory:  java -Xmx1500m MapBenchmark   /   java -Xmx2g PharmacyRedesign)`));
C.push(gap());
C.push(P("Limitations: all measurements come from a single-core machine with one JVM instance per program; small timings (below about 1 ms), the first (warm-up sensitive) rows of each table and any single large outlier should not be over-interpreted. The conclusions rest on growth rates and operation counts, which were stable across the runs."));

// ================= build =================
const doc = new Document({
  creator: "Student", title: "Programming in Java \u2013 Assignment 2 (CO2)",
  styles: {
    default: { document: { run: { font: FONT, size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: FONT, size: 32, bold: true, color: "1F3864" }, paragraph: { spacing: { before: 240, after: 160 }, keepNext: true, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: FONT, size: 26, bold: true, color: "2F5496" }, paragraph: { spacing: { before: 200, after: 100 }, keepNext: true, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true, run: { font: FONT, size: 23, bold: true, color: "404040" }, paragraph: { spacing: { before: 140, after: 80 }, keepNext: true, outlineLevel: 2 } },
    ],
  },
  numbering: { config: [{ reference: "bul", levels: [{ level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 540, hanging: 270 } } } }] }] },
  sections: [{
    properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1134, bottom: 1134, left: 1134, right: 1134 } } },
    footers: { default: new Footer({ children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "CSA09 \u2013 Assignment 2 (CO2) \u2013 Page ", font: FONT, size: 18 }), new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 18 })] })] }) },
    children: C,
  }],
});
Packer.toBuffer(doc).then((b) => { fs.writeFileSync(ROOT + "Assignment2_CO2_Report.docx", b); console.log("written", b.length); });

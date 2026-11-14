# CSA09-<RegisterNumber>-Assignment2  (Programming in Java – CO2)

| | |
|---|---|
| **Name** | <Your Name> |
| **Register Number** | <RegisterNumber> |
| **Java version used** | <fill in from `java -version`; results here were produced on OpenJDK 21> |
| **GitHub link** | https://github.com/<your-username>/CSA09-<RegisterNumber>-Assignment2 |

## How to compile and run (from inside each task folder)

| Task | Folder | Compile | Run | Result in one line |
|---|---|---|---|---|
| Q1 | `Task01` | `javac StringBenchmark.java` | `java StringBenchmark` | `+` in a loop is O(n²); StringBuilder O(n). StringBuffer for the shared log. |
| Q2 | `Task02` | `javac ListBenchmark.java` | `java ListBenchmark` | ArrayList wins get(k)/append; LinkedList loses badly in the middle; ArrayDeque wins for "Up Next". |
| Q3 | `Task03/before` | `javac Attendance.java` | `java Attendance` | Prints **3** (hashCode missing). |
| Q3 | `Task03/after` | `javac Attendance.java` | `java Attendance` | Prints **2** (TODO 1 completed). |
| Q3 | `Task03` | `javac HashContractTests.java` | `java HashContractTests` | 4 designed tests + alternatives. |
| Q4 | `Task04` | `javac MapBenchmark.java` | `java -Xmx1500m MapBenchmark` | HashMap/LinkedHashMap for lookups; TreeMap for code order. |
| Q5 | `Task05` | `javac LeaderboardDemo.java` | `java LeaderboardDemo` | `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]` |
| Q5 | `Task05` | `javac RankingDesign.java` | `java RankingDesign` | Comparator-chain design, contract checker, faulty comparators. |
| Q6 | `Task06` | `javac Triage.java` | `java Triage` | `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)` |
| Q6 | `Task06` | `javac TriageStudy.java` | `java TriageStudy` | Hand check, growth, starvation + aging. |
| Q7 | `Task07` | `javac PharmacyRedesign.java` | `java -Xmx2g PharmacyRedesign` | Cached sorted array + merge beats re-sorting. (takes several minutes) |

Each task folder contains: source code, `*_output.txt` (console output), `*_results.csv` (raw data) and a `screenshots/` folder
for **your own** output screenshots (take them when you run the programs on your machine).

> Timings depend on the machine. Re-run on your own computer and use your own numbers if they differ from the report.

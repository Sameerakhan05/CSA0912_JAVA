import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class PassStore {
    private final Path file;
    public PassStore(Path file) { this.file = file; }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            // CSV quoting: quote every field and double any embedded quote.
            String[] fields = {passId, regNo, name, String.valueOf(route), String.valueOf(amount)};
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) w.write(",");
                w.write("\"" + fields[i].replace("\"", "\"\"") + "\"");
            }
            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                try {
                    String[] f = parseCsv(line);
                    if (f.length != 5) continue;
                    Integer.parseInt(f[3]);
                    Double.parseDouble(f[4]);
                    if (f[0].equals(passId)) return f;
                } catch (RuntimeException ex) {
                    // Skip malformed records and continue scanning.
                }
            }
        }
        return null;
    }

    public void listByRoute(int route) throws IOException {
        if (!Files.exists(file)) return;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                try {
                    String[] f = parseCsv(line);
                    if (f.length == 5 && Integer.parseInt(f[3]) == route)
                        System.out.println(Arrays.toString(f));
                } catch (RuntimeException ex) { /* skip damaged line */ }
            }
        }
    }

    // Small CSV parser supporting quoted commas and doubled quote characters.
    static String[] parseCsv(String line) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                out.add(field.toString()); field.setLength(0);
            } else field.append(c);
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quote");
        out.add(field.toString());
        return out.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        Path f = Paths.get("passes.csv");
        Files.deleteIfExists(f);
        PassStore s = new PassStore(f);
        s.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        s.add("P1003", "192311003", "Divya", 7, 3800);
        System.out.println("Find P1003: " + Arrays.toString(s.find("P1003")));
        System.out.println("Find P9999: " + s.find("P9999"));
        System.out.println("Route 7:");
        s.listByRoute(7);
    }
}

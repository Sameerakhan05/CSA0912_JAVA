import java.io.*;
import java.nio.file.*;
public class BufferedLineCounter {
    static long countBuffered(Path p) throws IOException {
        long count = 0;
        try (BufferedReader r = Files.newBufferedReader(p)) {
            while (r.readLine() != null) count++;
        }
        return count;
    }
    static long countUnbuffered(Path p) throws IOException {
        long count = 0;
        try (InputStream in = new FileInputStream(p.toFile())) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') count++;
        }
        return count;
    }
    public static void main(String[] args) throws Exception {
        Path p = Paths.get(args.length == 0 ? "attendance.txt" : args[0]);
        long t1 = System.nanoTime(), a = countUnbuffered(p), t2 = System.nanoTime();
        long b = countBuffered(p), t3 = System.nanoTime();
        System.out.printf("Unbuffered lines=%d time=%.3f ms%n", a, (t2-t1)/1e6);
        System.out.printf("Buffered lines=%d time=%.3f ms%n", b, (t3-t2)/1e6);
    }
}

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class PassMigration {
    // Usage: java -cp ".;mysql-connector-j.jar" PassMigration jdbc:mysql://localhost:3306/co5 user password passes.csv
    public static void main(String[] a) throws Exception {
        if (a.length < 4) {
            System.out.println("Usage: PassMigration <jdbc-url> <user> <password> <csv-file>");
            return;
        }
        List<String[]> good = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        int lineNo=0;
        try (BufferedReader r=Files.newBufferedReader(Paths.get(a[3]))) {
            String line;
            while ((line=r.readLine())!=null) {
                lineNo++;
                String[] f=line.split(",",-1);
                try {
                    if (f.length!=5 || f[0].isBlank() || f[1].isBlank() ||
                        f[2].isBlank()) throw new IllegalArgumentException("wrong fields/empty field");
                    Integer.parseInt(f[3]); Double.parseDouble(f[4]);
                    good.add(f);
                } catch (RuntimeException ex) { failed.add("Line "+lineNo+": "+line+" ("+ex.getMessage()+")"); }
            }
        }
        try (Connection c=DriverManager.getConnection(a[0],a[1],a[2])) {
            int count=PassLoader.loadAll(c,good);
            System.out.println("Successfully migrated: "+count);
            DatabaseMetaData md=c.getMetaData();
            System.out.println("Database: "+md.getDatabaseProductName());
            System.out.println("Driver: "+md.getDriverName()+" "+md.getDriverVersion());
            try (ResultSet rs=md.getTables(null,null,"%",new String[]{"TABLE"})) {
                System.out.println("Tables:");
                while(rs.next()) System.out.println(" - "+rs.getString("TABLE_NAME"));
            }
        }
        System.out.println("Rejected records: "+failed.size());
        for(String s:failed) System.out.println(s);
    }
}

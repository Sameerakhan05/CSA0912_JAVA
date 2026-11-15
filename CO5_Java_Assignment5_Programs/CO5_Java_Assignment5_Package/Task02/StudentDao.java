import java.sql.*;
import java.util.*;
public class StudentDao implements Dao<Student, Integer> {
    private final Connection con;
    public StudentDao(Connection con) { this.con = con; }

    public void insert(Student s) throws SQLException {
        String q = "INSERT INTO student(student_id, name, email) VALUES(?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setInt(1, s.getStudentId()); ps.setString(2, s.getName());
            ps.setString(3, s.getEmail()); ps.executeUpdate();
        }
    }
    public Optional<Student> findById(Integer id) throws SQLException {
        String q = "SELECT student_id,name,email FROM student WHERE student_id=?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setInt(1,id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(new Student(rs.getInt(1),rs.getString(2),rs.getString(3)));
            }
        }
        return Optional.empty();
    }
    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT student_id,name,email FROM student")) {
            while (rs.next()) list.add(new Student(rs.getInt(1),rs.getString(2),rs.getString(3)));
        }
        return list;
    }
    public void update(Student s) throws SQLException {
        String q = "UPDATE student SET name=?,email=? WHERE student_id=?";
        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setString(1,s.getName()); ps.setString(2,s.getEmail());
            ps.setInt(3,s.getStudentId()); ps.executeUpdate();
        }
    }
    public void delete(Integer id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM student WHERE student_id=?")) {
            ps.setInt(1,id); ps.executeUpdate();
        }
    }
}

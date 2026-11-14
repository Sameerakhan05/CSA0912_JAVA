import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.*;

public class EventRegistrationApp extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_events";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";
    private final JTextField studentId = new JTextField(12);
    private final JTextField studentName = new JTextField(12);
    private final JComboBox<String> event = new JComboBox<>(new String[]{"Tech Fest", "Cultural Fest", "Sports Meet"});
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Tech Fest", "Cultural Fest", "Sports Meet"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Student Name", "Event"}, 0);
    private final JTable table = new JTable(model);
    private int offset = 0;
    private static final int PAGE_SIZE = 100;

    public EventRegistrationApp() {
        super("Campus Event Registration");
        setLayout(new BorderLayout(8,8));
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem refresh = new JMenuItem("Refresh");
        JMenuItem exit = new JMenuItem("Exit");
        refresh.addActionListener(e -> loadPage(true));
        exit.addActionListener(e -> dispose());
        file.add(refresh); file.add(exit); bar.add(file); setJMenuBar(bar);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Student ID")); form.add(studentId);
        form.add(new JLabel("Name")); form.add(studentName);
        form.add(new JLabel("Event")); form.add(event);
        JButton register = new JButton("Register");
        form.add(register);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton applyFilter = new JButton("Apply Filter");
        JButton previous = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton export = new JButton("Export CSV");
        filterPanel.add(new JLabel("Filter")); filterPanel.add(filter);
        filterPanel.add(applyFilter); filterPanel.add(previous); filterPanel.add(next); filterPanel.add(export);
        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.NORTH); north.add(filterPanel, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        register.addActionListener(e -> registerStudent());
        applyFilter.addActionListener(e -> { offset=0; loadPage(true); });
        previous.addActionListener(e -> { offset=Math.max(0, offset-PAGE_SIZE); loadPage(false); });
        next.addActionListener(e -> { offset+=PAGE_SIZE; loadPage(false); });
        export.addActionListener(e -> exportCsv());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850,500); setLocationRelativeTo(null);
        loadPage(true);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void registerStudent() {
        String id=studentId.getText().trim(), name=studentName.getText().trim();
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Student ID and name are required."); return;
        }
        String sql="INSERT INTO registrations(student_id, student_name, event_name) VALUES(?,?,?)";
        new SwingWorker<String,Void>() {
            protected String doInBackground() {
                try (Connection c=connect(); PreparedStatement p=c.prepareStatement(sql)) {
                    p.setString(1,id); p.setString(2,name); p.setString(3,(String)event.getSelectedItem());
                    p.executeUpdate(); return "Registration successful.";
                } catch (SQLIntegrityConstraintViolationException ex) {
                    return "Duplicate registration: this student is already registered for this event.";
                } catch (SQLException ex) { return "Database error: "+ex.getMessage(); }
            }
            protected void done() {
                try { JOptionPane.showMessageDialog(EventRegistrationApp.this,get()); loadPage(true); }
                catch(Exception ex) { JOptionPane.showMessageDialog(EventRegistrationApp.this,"Unable to register."); }
            }
        }.execute();
    }

    private void loadPage(boolean reset) {
        String selected=(String)filter.getSelectedItem();
        new SwingWorker<Object[][],Void>() {
            protected Object[][] doInBackground() throws Exception {
                String sql = "All".equals(selected)
                    ? "SELECT student_id,student_name,event_name FROM registrations ORDER BY student_id LIMIT ? OFFSET ?"
                    : "SELECT student_id,student_name,event_name FROM registrations WHERE event_name=? ORDER BY student_id LIMIT ? OFFSET ?";
                try (Connection c=connect(); PreparedStatement p=c.prepareStatement(sql)) {
                    int i=1;
                    if (!"All".equals(selected)) p.setString(i++,selected);
                    p.setInt(i++,PAGE_SIZE); p.setInt(i,offset);
                    try(ResultSet rs=p.executeQuery()) {
                        java.util.List<Object[]> rows=new java.util.ArrayList<>();
                        while(rs.next()) rows.add(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3)});
                        return rows.toArray(new Object[0][]);
                    }
                }
            }
            protected void done() {
                try {
                    model.setRowCount(0);
                    for(Object[] row:get()) model.addRow(row);
                } catch(Exception ex) { JOptionPane.showMessageDialog(EventRegistrationApp.this,"Could not load records: "+ex.getMessage()); }
            }
        }.execute();
    }


    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter out = new java.io.PrintWriter(chooser.getSelectedFile())) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) out.print(",");
                out.print(model.getColumnName(c));
            }
            out.println();
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) out.print(",");
                    String value = String.valueOf(model.getValueAt(r, c)).replace("\"", "\"\"");
                    out.print("\"" + value + "\"");
                }
                out.println();
            }
            JOptionPane.showMessageDialog(this, "Current table page exported successfully.");
        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventRegistrationApp().setVisible(true));
    }
}

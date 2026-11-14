import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

class FeeService {
    private static final String URL = "jdbc:mysql://localhost:3306/college";
    private static final String USER = "root";
    private static final String PWD = "your_password";

    public String pay(String regNo, String amountText) {
        if (regNo == null || regNo.trim().isEmpty()) return "Enter registration number.";
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) return "Amount must be greater than zero.";
        } catch (NumberFormatException ex) {
            return "Enter a valid amount.";
        }

        String sql = "INSERT INTO fees(reg_no, amount) VALUES (?, ?)";
        try (Connection c = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            ps.setDouble(2, amount);
            ps.executeUpdate();
            return "Payment recorded successfully.";
        } catch (SQLException ex) {
            return "Payment failed: " + ex.getMessage();
        }
    }
}

public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(15);
    private final JTextField amount = new JTextField(15);
    private final JLabel status = new JLabel("Enter payment details.");
    private final FeeService service = new FeeService();

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new GridLayout(4, 2, 8, 8));
        add(new JLabel("Registration No:")); add(regNo);
        add(new JLabel("Amount:")); add(amount);
        JButton pay = new JButton("Pay");
        JButton clear = new JButton("Clear");
        JButton receipt = new JButton("Receipt");
        add(pay); add(clear); add(receipt); add(status);

        pay.addActionListener(e -> {
            pay.setEnabled(false);
            status.setText("Processing...");
            new SwingWorker<String, Void>() {
                protected String doInBackground() {
                    return service.pay(regNo.getText(), amount.getText());
                }
                protected void done() {
                    try { status.setText(get()); }
                    catch (Exception ex) { status.setText("Unable to complete payment."); }
                    pay.setEnabled(true);
                }
            }.execute();
        });
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); status.setText("Cleared."); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Registration: " + regNo.getText() + "\nAmount: " + amount.getText(),
                "Receipt Preview", JOptionPane.INFORMATION_MESSAGE));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(430, 220);
        setLocationRelativeTo(null);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConverterFrameListener extends JFrame implements ActionListener {
    JTextField amount = new JTextField(10);
    JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    JLabel result = new JLabel(" ");
    public ConverterFrameListener() {
        super("Converter - Frame Listener");
        setLayout(new FlowLayout());
        JButton convert = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target); add(convert); add(result);
        convert.addActionListener(this);
        amount.addActionListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE); pack(); setLocationRelativeTo(null);
    }
    public void actionPerformed(ActionEvent e) { convert(); }
    private void convert() {
        try {
            double inr = Double.parseDouble(amount.getText());
            double rate = target.getSelectedItem().equals("USD") ? 0.012 : 0.011;
            result.setText(String.format("%.2f %s", inr * rate, target.getSelectedItem()));
            JOptionPane.showMessageDialog(this, result.getText());
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterFrameListener().setVisible(true)); }
}

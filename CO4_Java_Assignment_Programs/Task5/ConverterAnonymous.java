import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ConverterAnonymous extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    public ConverterAnonymous() {
        super("Converter - Anonymous Inner Class");
        setLayout(new FlowLayout());
        JButton convert = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target); add(convert); add(result);
        ActionListener handler = new ActionListener() {
            public void actionPerformed(ActionEvent e) { convert(); }
        };
        convert.addActionListener(handler); amount.addActionListener(handler);
        setDefaultCloseOperation(EXIT_ON_CLOSE); pack(); setLocationRelativeTo(null);
    }
    private void convert() {
        try {
            double v = Double.parseDouble(amount.getText());
            double r = target.getSelectedItem().equals("USD") ? .012 : .011;
            result.setText(String.format("%.2f %s", v*r, target.getSelectedItem()));
            JOptionPane.showMessageDialog(this, result.getText());
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterAnonymous().setVisible(true)); }
}

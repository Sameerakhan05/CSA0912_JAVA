import java.awt.*;
import javax.swing.*;

public class LibrarySearch extends JFrame {
    public LibrarySearch() {
        super("Campus Library Search");
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Campus Library", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(22);
        Choice category = new Choice();
        category.add("All"); category.add("Book"); category.add("Journal");
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search:")); searchPanel.add(search);
        searchPanel.add(category); searchPanel.add(searchButton);

        JTextArea results = new JTextArea();
        results.setEditable(false);
        results.setText("Search results will appear here.");
        add(searchPanel, BorderLayout.WEST);
        add(new JScrollPane(results), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout());
        JButton prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
        JLabel status = new JLabel("Ready");
        bottom.add(prev); bottom.add(next); bottom.add(reserve); bottom.add(status);
        add(bottom, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> {
            results.setText("Searching for: " + search.getText()
                    + "\\nCategory: " + category.getSelectedItem()
                    + "\\nSample result: Java Programming");
            status.setText("Search complete");
        });
        prev.addActionListener(e -> status.setText("Previous page selected"));
        next.addActionListener(e -> status.setText("Next page selected"));
        reserve.addActionListener(e -> status.setText("Select a result before reserving"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 450);
        setLocationRelativeTo(null);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true));
    }
}

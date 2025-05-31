import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TextFileFilterApp extends JFrame {
    private JTextField filePathField;
    private JTextField keywordField;
    private JButton browseButton;
    private JButton exportButton;

    public TextFileFilterApp() {
        setTitle("Text File Filter");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // File selection components
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Select Text File:"), gbc);

        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        add(filePathField, gbc);

        browseButton = new JButton("Browse");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        add(browseButton, gbc);

        // Keyword input components
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Enter Keyword:"), gbc);

        keywordField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(keywordField, gbc);

        // Export button
        exportButton = new JButton("Filter and Export");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        add(exportButton, gbc);

        // Add action listeners
        browseButton.addActionListener(new BrowseAction());
        exportButton.addActionListener(new ExportAction());

        setLocationRelativeTo(null);
    }

    private class BrowseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

            int result = fileChooser.showOpenDialog(TextFileFilterApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private class ExportAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String inputPath = filePathField.getText();
            String keyword = keywordField.getText().trim();

            if (inputPath.isEmpty() || keyword.isEmpty()) {
                JOptionPane.showMessageDialog(TextFileFilterApp.this,
                        "Please select a file and enter a keyword",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            File inputFile = new File(inputPath);
            if (!inputFile.exists() || !inputFile.isFile()) {
                JOptionPane.showMessageDialog(TextFileFilterApp.this,
                        "Selected file does not exist",
                        "File Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Read and filter lines
                List<String> allLines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);
                List<String> filteredLines = new ArrayList<>();

                for (String line : allLines) {
                    if (line.contains(keyword)) {
                        filteredLines.add(line);
                    }
                }

                if (filteredLines.isEmpty()) {
                    JOptionPane.showMessageDialog(TextFileFilterApp.this,
                            "No lines found containing the keyword",
                            "No Results",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Create output filename
                String outputFileName = getOutputFileName(inputFile, keyword);
                File outputFile = new File(inputFile.getParent(), outputFileName);

                // Write to output file
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

                    for (String line : filteredLines) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                JOptionPane.showMessageDialog(TextFileFilterApp.this,
                        "Exported " + filteredLines.size() + " lines to:\n" + outputFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(TextFileFilterApp.this,
                        "Error processing file: " + ex.getMessage(),
                        "Processing Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private String getOutputFileName(File inputFile, String keyword) {
            String name = inputFile.getName();
            int dotIndex = name.lastIndexOf('.');

            String baseName = (dotIndex == -1) ? name : name.substring(0, dotIndex);
            String extension = (dotIndex == -1) ? ".txt" : name.substring(dotIndex);

            return baseName + "_filtered_" + sanitizeKeyword(keyword) + extension;
        }

        private String sanitizeKeyword(String keyword) {
            // Remove special characters that might be invalid in filenames
            return keyword.replaceAll("[^a-zA-Z0-9_-]", "");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TextFileFilterApp().setVisible(true);
        });
    }
}

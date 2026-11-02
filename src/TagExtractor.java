import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

public class TagExtractor extends JFrame
{
    private final JTextField textFileField = new JTextField();
    private final JTextField stopFileField = new JTextField();
    private final JTextArea resultsArea = new JTextArea();
    private final JButton saveButton = new JButton("Save Results");

    private Map<String, Integer> currentResults = new TreeMap<>();

    public TagExtractor()
    {
        super("Tag / Keyword Extractor");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);

        JButton chooseTextButton = new JButton("Choose Text File");
        JButton chooseStopButton = new JButton("Choose Stop-Word File");
        JButton processButton = new JButton("Extract Tags");

        chooseTextButton.addActionListener(e ->
                chooseFile(textFileField, "Select literature text"));

        chooseStopButton.addActionListener(e ->
                chooseFile(stopFileField, "Select stop-word list"));

        processButton.addActionListener(e -> extractTags());
        saveButton.addActionListener(e -> saveResults());

        JPanel filePanel = new JPanel(new GridLayout(2, 3, 8, 8));

        filePanel.add(new JLabel("Text file:"));
        filePanel.add(textFileField);
        filePanel.add(chooseTextButton);

        filePanel.add(new JLabel("Stop words:"));
        filePanel.add(stopFileField);
        filePanel.add(chooseStopButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(processButton);
        buttonPanel.add(saveButton);

        add(filePanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.setEnabled(false);
    }

    private void chooseFile(JTextField target, String title)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            target.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void extractTags()
    {
        if (textFileField.getText().isEmpty() || stopFileField.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(this,
                    "Choose both the text file and stop-word file first.",
                    "Missing file",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try
        {
            Set<String> stopWords = new HashSet<>();

            Scanner stopScanner = new Scanner(new File(stopFileField.getText()));

            while (stopScanner.hasNextLine())
            {
                String word = stopScanner.nextLine().trim().toLowerCase();

                if (!word.isEmpty())
                {
                    stopWords.add(word);
                }
            }
            stopScanner.close();

            Map<String, Integer> frequencies = new TreeMap<>();

            Scanner textScanner = new Scanner(new File(textFileField.getText()));

            while (textScanner.hasNext())
            {
                String word = textScanner.next().toLowerCase().replaceAll("[^a-z]", " ");

                if (!word.isEmpty() && !stopWords.contains(word))
                {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }

            textScanner.close();

            currentResults = frequencies;

            StringBuilder display = new StringBuilder();
            display.append(String.format("%-24s %s%n", "TAG", "FREQUENCY"));
            display.append("-----------------------------------\n");

            for (Map.Entry<String, Integer> entry: frequencies.entrySet())
            {
                display.append(String.format("%-24s %d%n", entry.getKey(), entry.getValue()));
            }

            resultsArea.setText(display.toString());
            saveButton.setEnabled(!frequencies.isEmpty());

        }
        catch (FileNotFoundException ex)
        {
            JOptionPane.showMessageDialog(this,
                    "Could not find a selected file: " + ex.getMessage(),
                    "File error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveResults()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save extracted tags");
        chooser.setSelectedFile(new File("tags_output.txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = chooser.getSelectedFile();

            try
            {
                PrintWriter writer = new PrintWriter(file);

                writer.println("TAG\tFREQUENCY");

                for (Map.Entry<String, Integer> entry : currentResults.entrySet())
                {
                    writer.println(entry.getKey() + "\t" + entry.getValue());
                }

                writer.close();

                JOptionPane.showMessageDialog(
                        this,
                        "Results saved to " + file.getAbsolutePath());

            }
            catch (FileNotFoundException ex)
            {
                JOptionPane.showMessageDialog(this,
                        "Could not save results: " + ex.getMessage(),
                        "Save error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args)
    {
        new TagExtractor().setVisible(true);
    }
}
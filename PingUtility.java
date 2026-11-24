import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingUtility extends JFrame {

    private JTextField hostField;
    private JTextArea outputArea;
    private JButton startButton;
    private JButton stopButton;

    private Process pingProcess;
    private Thread pingThread;

    public PingUtility() {
        setTitle("Ping Utility");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        hostField = new JTextField("8.8.8.8");
        startButton = new JButton("Start Ping");
        stopButton = new JButton("Stop");

        topPanel.add(new JLabel("Host/IP:"), BorderLayout.WEST);
        topPanel.add(hostField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(e -> startPing());
        stopButton.addActionListener(e -> stopPing());
    }

    private void startPing() {
        String host = hostField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a hostname or IP address.");
            return;
        }

        startButton.setEnabled(false);
        outputArea.setText("");

        pingThread = new Thread(() -> {
            try {

                boolean isWindows =
                        System.getProperty("os.name").toLowerCase().contains("win");

                String[] command;

                if (isWindows) {
                    command = new String[]{"ping", "-t", host};
                } else {
                    command = new String[]{"ping", host};
                }

                ProcessBuilder pb = new ProcessBuilder(command);
                pingProcess = pb.start();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        pingProcess.getInputStream()));

                String line;

                while ((line = reader.readLine()) != null) {
                    String finalLine = line;

                    SwingUtilities.invokeLater(() -> {
                        outputArea.append(finalLine + "\n");
                        outputArea.setCaretPosition(
                                outputArea.getDocument().getLength());
                    });
                }

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        outputArea.append("Error: " + ex.getMessage() + "\n"));
            } finally {
                SwingUtilities.invokeLater(() ->
                        startButton.setEnabled(true));
            }
        });

        pingThread.start();
    }

    private void stopPing() {
        try {
            if (pingProcess != null) {
                pingProcess.destroy();
                outputArea.append("\nPing stopped.\n");
            }
            startButton.setEnabled(true);
        } catch (Exception ex) {
            outputArea.append("Error stopping ping.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PingUtility gui = new PingUtility();
            gui.setVisible(true);
        });
    }
}

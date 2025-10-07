package main;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private final JTextArea area = new JTextArea();
    private final ChatServer server = new ChatServer();

    public ServerGUI() {
        setTitle("Server (Erdo)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 400);
        setLayout(new BorderLayout());

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton startButton = new JButton("Sunucuyu Baslat");
        add(startButton, BorderLayout.SOUTH);

        startButton.addActionListener(e -> {
            try {
                server.startServer(12345, message ->
                    SwingUtilities.invokeLater(() -> area.append(message + "\n"))
                );
                area.append("Sunucu baslatildi. Port 12345\n");
                startButton.setEnabled(false);
            } catch (Exception ex) {
                area.append("Sunucu baslatilamadi: " + ex.getMessage() + "\n");
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}

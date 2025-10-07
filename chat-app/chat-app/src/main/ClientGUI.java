package main;

import main.encryption.*;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private final ChatClient client = new ChatClient();
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final JTextField keyField = new JTextField();
    private final JComboBox<String> encryptionSelect;

    private EncryptionAlgorithm selectedAlgorithm;

    public ClientGUI() {
        setTitle("Client (Oya)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setLayout(new BorderLayout());

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        // Mesaj ve Key input paneli
        JPanel southPanel = new JPanel(new BorderLayout());

        // Mesaj input
        input.setToolTipText("Mesajinizi buraya yazin");
        southPanel.add(input, BorderLayout.CENTER);

        // Key input
        keyField.setToolTipText("Sifreleme key degerini buraya yazin");
        keyField.setPreferredSize(new Dimension(100, 30));
        southPanel.add(keyField, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        // Dropdown paneli
        encryptionSelect = new JComboBox<>(new String[]{
                "AffineCipher", "SezarSifreleme", "SubstitutionCipher", "VigenereCipher"
        });
        add(encryptionSelect, BorderLayout.NORTH);

        encryptionSelect.addActionListener(e -> updateAlgorithm());

        input.addActionListener(e -> {
            String msg = input.getText().trim();
            String key = keyField.getText().trim();

            if (!msg.isEmpty()) {
                try {
                    if (selectedAlgorithm != null) {
                        String encrypted = selectedAlgorithm.encrypt(msg);
                        client.sendMessage(encrypted);
                        area.append("Ben: " + msg + " (Sifreli: " + encrypted + ")\n");
                        input.setText("");
                    } else {
                        area.append("Lutfen gecerli bir algoritma ve key girin.\n");
                    }
                } catch (Exception ex) {
                    area.append("Sifreleme hatasi: " + ex.getMessage() + "\n");
                }
            }
        });

        try {
            client.connect("127.0.0.1", 12345);
            area.append("Sunucuya baglandi.\n");
        } catch (Exception ex) {
            area.append("Baglanti hatasi: " + ex.getMessage() + "\n");
        }

        setVisible(true);
        updateAlgorithm();
    }

    private void updateAlgorithm() {
        String selected = (String) encryptionSelect.getSelectedItem();
        String key = keyField.getText().trim();

        try {
            switch (selected) {
                case "AffineCipher":
                    if (!key.contains(",")) throw new Exception("Affine icin key formati: a,b");
                    String[] parts = key.split(",");
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    selectedAlgorithm = new AffineCipher(a, b);
                    break;
                case "SezarSifreleme":
                    selectedAlgorithm = new SezarSifreleme(Integer.parseInt(key));
                    break;
                case "SubstitutionCipher":
                    selectedAlgorithm = new SubstitutionCipher(key);
                    break;
                case "VigenereCipher":
                    selectedAlgorithm = new VigenereCipher(key);
                    break;
            }
        } catch (Exception e) {
            selectedAlgorithm = null;
            area.append("Key hatali: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}

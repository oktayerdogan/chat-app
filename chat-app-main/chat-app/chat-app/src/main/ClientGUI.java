package main;

import main.encryption.*;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    
    private final ChatClient client;
    
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final JTextField keyField = new JTextField();
    
    private final JComboBox<String> encryptionSelect = new JComboBox<>(new String[]{
        "AffineCipher", "SezarSifreleme", "SubstitutionCipher", "VigenereCipher"
    });

    private EncryptionAlgorithm selectedAlgorithm;

    public ClientGUI(String serverAddress, int portNumber) {

        this.client = new ChatClient(); 
        
        try {
            client.connect(serverAddress, portNumber); 
            area.append("Sunucuya baglandi: " + serverAddress + ":" + portNumber + "\n");
        } catch (Exception ex) {
            area.append("Baglanti hatasi: " + ex.getMessage() + "\n");
        }
        
        initializeGUI();
        
        updateAlgorithm();
    }

    private void initializeGUI() {
        setTitle("Client (Oya)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 500);
        setLayout(new BorderLayout());

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());

        input.setToolTipText("Mesajinizi buraya yazin");
        southPanel.add(input, BorderLayout.CENTER);

        keyField.setToolTipText("Sifreleme key degerini buraya yazin");
        keyField.setPreferredSize(new Dimension(100, 30));
        southPanel.add(keyField, BorderLayout.EAST);

        add(southPanel, BorderLayout.SOUTH);

        add(encryptionSelect, BorderLayout.NORTH);

        encryptionSelect.addActionListener(e -> updateAlgorithm());

        input.addActionListener(e -> {
            String msg = input.getText().trim();
            String key = keyField.getText().trim();

            if (!msg.isEmpty()) {
                try {
                    updateAlgorithm(); 
                    
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
            area.append("Key hatali: " + selected + " -> " + e.getMessage() + "\n");
        }
    }

    // Mesaj alma olaylarında kullanılmak üzere bir metot ekleyebilirsiniz.
    public void displayMessage(String sender, String message) {
        // Alınan şifreli mesajı burada gösterme/çözme mantığı olmalı
        area.append(sender + ": " + message + "\n");
    }
}
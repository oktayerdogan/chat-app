package main;

import main.encryption.*; 
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private final ChatClient client;

    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final JTextField keyField = new JTextField();

    private final JTextField portField = new JTextField("5000");
    private final JButton connectButton = new JButton("Bağlan");
    private final JButton fileButton = new JButton("Dosya Gönder");

    // 🔹 GCDCipher menüye eklendi
    private final JComboBox<String> encryptionSelect = new JComboBox<>(new String[]{
        "Şifresiz Gönder", 
        "AffineCipher", 
        "SezarSifreleme", 
        "SubstitutionCipher", 
        "VigenereCipher", 
        "RouteCipher", 
        "ColumnarTransposition",
        "PolybiusCipher",
        "HillCipher",
        "GCDCipher"
    });

    private EncryptionAlgorithm selectedAlgorithm;

    public ClientGUI() {
        this.client = new ChatClient(); 
        initializeGUI();
        updateAlgorithm(); 
    }

    private String guessMimeType(String fileName) {
        String ext = fileName.toLowerCase();
        if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")) return "image/jpeg";
        if (ext.endsWith(".png")) return "image/png";
        if (ext.endsWith(".gif")) return "image/gif";
        if (ext.endsWith(".mp4")) return "video/mp4";
        if (ext.endsWith(".pdf")) return "application/pdf";
        if (ext.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    private void initializeGUI() {
        setTitle("Client (Oya)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 500);
        setLayout(new BorderLayout());

        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Port:"));
        portField.setPreferredSize(new Dimension(60, 25));
        topPanel.add(portField);
        topPanel.add(connectButton);
        topPanel.add(encryptionSelect);
        topPanel.add(fileButton);
        add(topPanel, BorderLayout.NORTH);

        fileButton.addActionListener(e -> sendFile());

        JPanel southPanel = new JPanel(new BorderLayout());
        input.setToolTipText("Mesajinizi buraya yazin");
        southPanel.add(input, BorderLayout.CENTER);
        keyField.setToolTipText("Sifreleme key degerini buraya yazin");
        keyField.setPreferredSize(new Dimension(100, 30));
        southPanel.add(keyField, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);

        encryptionSelect.addActionListener(e -> updateAlgorithm());
        connectButton.addActionListener(e -> connectToServer());
        input.addActionListener(e -> sendMessage());

        new Thread(this::receiveMessages, "Client-Receive-Thread").start();
    }

    private void sendMessage() {
        String msg = input.getText().trim();
        if (!msg.isEmpty()) {
            try {
                updateAlgorithm(); 
                
                if (!client.isConnected()) {
                    area.append("Hata: Sunucuya bagli degil. Mesaj gonderilemiyor.\n");
                    return;
                }

                String toSend = (selectedAlgorithm != null) ? selectedAlgorithm.encrypt(msg) : msg;
                client.sendMessage(toSend);

                if (selectedAlgorithm != null) {
                    area.append("Ben: " + msg + " (Şifreli: " + toSend + ")\n");
                } else {
                    area.append("Ben: " + msg + " (Şifresiz gönderildi)\n");
                }

                input.setText("");
            } catch (Exception ex) {
                area.append("Mesaj gönderme hatası: " + ex.getMessage() + "\n");
            }
        }
    }

    private void sendFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                String fileName = file.getName();
                String mimeType = guessMimeType(fileName);
                byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());

                updateAlgorithm();
                String fileText = java.util.Base64.getEncoder().encodeToString(fileBytes);
                String toSend = (selectedAlgorithm != null) ? selectedAlgorithm.encrypt(fileText) : fileText;

                client.sendFile(fileName, mimeType, toSend);

                if (selectedAlgorithm != null) {
                    area.append("Şifreli dosya gönderildi: " + fileName + "\n");
                } else {
                    area.append("Dosya şifresiz gönderildi: " + fileName + "\n");
                }
            } catch (Exception ex) {
                area.append("Dosya gönderme hatası: " + ex.getMessage() + "\n");
            }
        }
    }

    private void connectToServer() {
        String portStr = portField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            area.append("Port numarasi hatali!\n");
            return;
        }
        connectButton.setEnabled(false);
        area.append("Sunucuya baglaniyor: localhost:" + port + "\n");
        new Thread(() -> {
            try {
                client.connect("127.0.0.1", port);
                SwingUtilities.invokeLater(() -> {
                    area.append("Sunucuya baglandi: localhost:" + port + "\n");
                    connectButton.setEnabled(false);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    area.append("Baglanti hatasi: " + ex.getMessage() + "\n");
                    connectButton.setEnabled(true);
                });
            }
        }, "Client-Connect-Thread").start();
    }

    private void receiveMessages() {
        try {
            java.io.BufferedReader in = new java.io.BufferedReader(
                new java.io.InputStreamReader(client.getSocket().getInputStream())
            );
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("FILE:")) {
                    String[] parts = line.split(":", 4);
                    if (parts.length == 4) {
                        handleIncomingFile(parts[1], parts[2], parts[3]);
                    }
                } else {
                    area.append("[Gelen Mesaj] " + line + "\n");
                }
            }
        } catch (Exception ex) {
            area.append("Sunucudan veri okuma hatası: " + ex.getMessage() + "\n");
        }
    }

    private void handleIncomingFile(String fileName, String mimeType, String encryptedBase64) {
        updateAlgorithm();
        String base64 = encryptedBase64;

        if (selectedAlgorithm != null) {
            try {
                base64 = selectedAlgorithm.decrypt(encryptedBase64);
            } catch (Exception ex) {
                area.append("[Dosya şifresi çözülemedi: " + ex.getMessage() + "]\n");
                return;
            }
        }

        try {
            byte[] fileBytes = java.util.Base64.getDecoder().decode(base64);
            if (mimeType.startsWith("image/")) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(fileBytes);
                javax.swing.JLabel imgLabel = new javax.swing.JLabel(icon);
                javax.swing.JOptionPane.showMessageDialog(this, imgLabel, "Gelen Fotoğraf: " + fileName, javax.swing.JOptionPane.PLAIN_MESSAGE);
                area.append("[Fotoğraf geldi: " + fileName + "]\n");
            } else {
                java.nio.file.Path outPath = java.nio.file.Paths.get("gelen_" + fileName);
                java.nio.file.Files.write(outPath, fileBytes);
                area.append("[Dosya geldi ve kaydedildi: " + outPath.toString() + "]\n");
            }
        } catch (Exception e) {
            area.append("Dosya kaydetme hatası: " + e.getMessage() + "\n");
        }
    }

    
    private void updateAlgorithm() {
    String selected = (String) encryptionSelect.getSelectedItem();
    String key = keyField.getText().trim();

    try {
        switch (selected) {
            case "Şifresiz Gönder":
                selectedAlgorithm = null;
                break;
            case "AffineCipher":
                if (!key.contains(",")) throw new Exception("Affine için key formatı: a,b");
                String[] parts = key.split(",");
                int a = Integer.parseInt(parts[0].trim());
                int b = Integer.parseInt(parts[1].trim());
                selectedAlgorithm = new AffineCipher(a, b);
                break;
            case "SezarSifreleme":
                if (key.isEmpty()) throw new Exception("Sezar için key boş olamaz");
                selectedAlgorithm = new SezarSifreleme(Integer.parseInt(key));
                break;
            case "SubstitutionCipher":
                if (key.length() != 26) throw new Exception("Substitution için key 26 harf olmalı");
                selectedAlgorithm = new SubstitutionCipher(key);
                break;
            case "VigenereCipher":
                if (key.isEmpty()) throw new Exception("Vigenere için key boş olamaz");
                selectedAlgorithm = new VigenereCipher(key);
                break;
            case "RouteCipher":
                if (key.isEmpty()) throw new Exception("RouteCipher için key boş olamaz");
                selectedAlgorithm = new RouteCipher(Integer.parseInt(key));
                break;
            case "ColumnarTransposition":
                if (key.isEmpty()) throw new Exception("ColumnarTransposition için key boş olamaz");
                selectedAlgorithm = new ColumnarTranspositionCipher(key);
                break;
            case "PolybiusCipher":
                selectedAlgorithm = new PolybiusCipher();
                break;
            case "HillCipher":
                if (key.isEmpty()) throw new Exception("HillCipher için key boş olamaz");
                selectedAlgorithm = new HillCipher(key);
                break;
            case "GCDCipher":
                if (!key.contains(",")) throw new Exception("GCD için key formatı: a,b");
                String[] gcdParts = key.split(",");
                if (gcdParts.length != 2) throw new Exception("GCD key iki sayıdan oluşmalı: a,b");
                int x = Integer.parseInt(gcdParts[0].trim());
                int y = Integer.parseInt(gcdParts[1].trim());
                selectedAlgorithm = new GCDCipher(x, y);
                break;
            default:
                selectedAlgorithm = null;
                break;
        }
    } catch (Exception e) {
        selectedAlgorithm = null;
        area.append("Key hatalı: " + selected + " -> " + e.getMessage() + "\n");
    }
}


    public void displayMessage(String sender, String message) {
        area.append(sender + ": " + message + "\n");
    }
}

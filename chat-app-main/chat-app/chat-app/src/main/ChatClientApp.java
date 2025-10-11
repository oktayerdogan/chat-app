package main;

import javax.swing.SwingUtilities;

public class ChatClientApp {
    
    // Sunucu ve port bilgilerini buraya taşıyoruz
    // Use localhost by default for local testing. Change to remote IP if needed.
    private static final String SERVER_IP = "10.239.120.86";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        
        // GUI'nin güvenli bir şekilde Event Dispatch Thread (EDT) üzerinde çalışmasını sağlar.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // ClientGUI artık yeni constructor'ı ile başlatılıyor.
                ClientGUI clientFrame = new ClientGUI(SERVER_IP, PORT);
                clientFrame.setVisible(true);
            }
        });
    }
}
package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer {

    private final int port;
    private final ServerGUI gui;
    private ServerSocket serverSocket;
    private final AtomicBoolean isRunning = new AtomicBoolean(false); 
    
    // Bağlı istemcileri takip etmek için liste
    private final List<ClientHandler> clients = new ArrayList<>(); 

    public ChatServer(int port, ServerGUI gui) {
        this.port = port;
        this.gui = gui;
    }
    
    // ServerGUI'nin logMessage metoduna erişim için getter
    public ServerGUI getGui() {
        return gui;
    }

    // Sunucuyu başlatan metot (Hata veren try bloğu tamamlandı)
    public void startServer() throws IOException {
        if (isRunning.get()) {
            gui.logMessage("Hata: Sunucu zaten çalışıyor.");
            return;
        }

        try {
            serverSocket = new ServerSocket(port);
            isRunning.set(true);
            gui.logMessage("Sunucu baslatildi, port: " + port);

            while (isRunning.get()) {
                gui.logMessage("Yeni baglanti bekleniyor...");
                Socket clientSocket = serverSocket.accept();
                
                // İstemci bağlandığında ClientHandler başlat
                ClientHandler newClient = new ClientHandler(clientSocket, this);
                clients.add(newClient); // Listeye ekle
                newClient.start();      // Thread'i başlat

                gui.logMessage("Yeni istemci baglandi: " + clientSocket.getInetAddress().getHostAddress() + ". Toplam istemci: " + clients.size());
            }
        } 
        // SENTAKS HATASINI GİDEREN CATCH BLOKU:
        catch (IOException e) { 
            if (isRunning.get()) {
                // Sunucu aktifken (kendi isteğimizle durdurulmadıysa) hata logla
                gui.logMessage("Sunucu dinleme hatası: " + e.getMessage());
            }
        } finally { 
            // Hata olsun veya olmasın temiz kapatma yap
            isRunning.set(false);
            closeServer();
        }
    }
    
    // ServerGUI'nin çağırdığı DURDURMA metodu (Eksik metot eklendi)
    public void stopServer() {
        if (!isRunning.get()) return;

        isRunning.set(false); // Çalışma döngüsünü sonlandır
        gui.logMessage("Sunucu kapatılıyor...");
        
        // Sunucu soketini kapatarak accept() metodunu bloklamayı sonlandır
        closeServer();
        
        // Tüm bağlı istemcileri de kapat (isteğe bağlı ama önerilir)
        for (ClientHandler client : clients) {
             // client.closeConnection(); // ClientHandler'da böyle bir metot tanımlayabilirsiniz
        }
        clients.clear();
    }
    
    // Durum kontrol metodu (Eksik metot eklendi)
    public boolean isRunning() {
        return isRunning.get();
    }
    
    // Güvenli kapatma işlemini gerçekleştiren yardımcı metot (Eksik metot eklendi)
    private void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            gui.logMessage("Sunucu kapatılırken hata: " + e.getMessage());
        }
        gui.logMessage("Sunucu başarıyla kapatıldı.");
    }


    public void removeClient(ClientHandler client) {
        clients.remove(client);
        gui.logMessage("İstemci ayrıldı. Kalan istemci: " + clients.size());
    }

    // ClientHandler'dan gelen mesajları işleyen ana metot
    public void handleIncomingMessage(String encryptedMessage, ClientHandler sender) {
        // Şifreli mesajı gönderen kullanıcı bilgisi ile birlikte sunucu loguna yaz
        gui.logMessage("Gelen Şifreli Mesaj [" + sender.getClientId() + "]: " + encryptedMessage);
        
        // Mesajı diğer tüm istemcilere dağıt
        broadcast(encryptedMessage); 
    }
    
    // Mesajı tüm bağlı istemcilere gönderir
    public void broadcast(String message) {
        // İstemciler listesinde gezinerek mesajı gönder
        for (ClientHandler client : clients) {
            // Eğer istemciye mesaj göndermek için bir kontrol mekanizmanız varsa buraya ekleyin
            client.sendMessage(message); 
        }
    }
}
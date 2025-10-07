package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Her istemci bağlantısını yöneten iş parçacığı (thread)
public class ClientHandler extends Thread {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter writer;
    private String clientId; // İstemci kimliği

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String clientMessage;
            
            while ((clientMessage = reader.readLine()) != null) {
                server.handleIncomingMessage(clientMessage, this);
            }
            
        } catch (IOException e) {
            server.getGui().logMessage("İstemci bağlantısı kesildi veya hata oluştu: " + e.getMessage());
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                // Kapatma hatası
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
    
    public String getClientId() {
        return clientId;
    }
}
package main;

import java.io.*;
import java.net.*;

public class ChatServer {
    private ServerSocket serverSocket;

    public void startServer(int port, MessageListener listener) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Sunucu baslatildi. Port: " + port);

        new Thread(() -> {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket, listener)).start();
                }
            } catch (IOException e) {
                System.out.println("Sunucu kapatildi: " + e.getMessage());
            }
        }).start();
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final MessageListener listener;

        public ClientHandler(Socket socket, MessageListener listener) {
            this.socket = socket;
            this.listener = listener;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String msg;
                while ((msg = in.readLine()) != null) {
                    listener.onMessage(msg);
                }
            } catch (IOException e) {
                System.out.println("İstemci baglantisi kesildi.");
            }
        }
    }

    public interface MessageListener {
        void onMessage(String message);
    }
}

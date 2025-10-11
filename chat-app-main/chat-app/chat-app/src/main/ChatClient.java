package main;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;

    /**
     * Connect with a small timeout so callers (especially GUIs) don't block indefinitely.
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket();
        // 3000 ms timeout
        socket.connect(new InetSocketAddress(host, port), 3000);
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        out = null;
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }
}

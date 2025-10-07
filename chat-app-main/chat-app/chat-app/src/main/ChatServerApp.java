package main;

public class ChatServerApp {
    public static void main(String[] args) {
        // Sunucu uygulamasını başlatan ana metod
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Burada ServerGUI sınıfının bir örneği oluşturulur ve görünür yapılır.
                new ServerGUI(5000).setVisible(true); 
                // ServerGUI'nin yapıcı metoduna port numarası geçilir.
            }
        });
    }
}
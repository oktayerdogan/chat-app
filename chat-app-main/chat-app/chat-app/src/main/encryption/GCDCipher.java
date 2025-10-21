package main.encryption;

public class GCDCipher implements EncryptionAlgorithm {
    private final int a;
    private final int b;

    // Key constructor ile verilecek
    public GCDCipher(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String encrypt(String text) {
        // GCD hesapla
        int gcd = computeGCD(a, b);
        // Mesajla birlikte göster
        return "GCD(" + a + ", " + b + ") = " + gcd + " → Mesaj: " + text;
    }

    @Override
    public String decrypt(String text) {
        // GCD algoritması ters çevrilemez
        return "GCD algoritması ters çevrilemez: " + text;
    }

    // Öklid algoritması
    private int computeGCD(int x, int y) {
        while (y != 0) {
            int temp = y;
            y = x % y;
            x = temp;
        }
        return Math.abs(x);
    }
}

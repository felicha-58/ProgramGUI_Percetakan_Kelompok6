package main.java.model;

public class NotaItem {
    private int id;
    private Cetakan cetakan;
    private int jumlah;

    public NotaItem(int id, Cetakan cetakan, int jumlah) {
        this.id = id; this.cetakan = cetakan; this.jumlah = jumlah;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cetakan getCetakan() { return cetakan; }
    public void setCetakan(Cetakan c) { this.cetakan = c; }

    public int getJumlah() { return jumlah; }
    public void setJumlah(int j) { this.jumlah = j; }

    public double getSubtotal() { return cetakan.hitungHarga(jumlah); }
}

package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Brosur extends Cetakan {
    private final IntegerProperty jumlahHalaman = new SimpleIntegerProperty();
    private final StringProperty jenisKertas = new SimpleStringProperty();

    public Brosur(int id, String kode, String nama, String ukuran, double harga,
                  int jumlahHalaman, String jenisKertas) {
        super(id, kode, nama, ukuran, harga);
        setJumlahHalaman(jumlahHalaman); setJenisKertas(jenisKertas);
    }

    @Override
    public boolean validate() {
        return getKode() != null && !getKode().trim().isEmpty()
            && getNama() != null && !getNama().trim().isEmpty()
            && getHargaSatuan() > 0 && getJumlahHalaman() > 0;
    }

    @Override
    public String getDetail() { return "Halaman: " + getJumlahHalaman() + ", Kertas: " + getJenisKertas(); }

    public IntegerProperty jumlahHalamanProperty() { return jumlahHalaman; }
    public int getJumlahHalaman() { return jumlahHalaman.get(); }
    public void setJumlahHalaman(int v) { jumlahHalaman.set(v); }

    public StringProperty jenisKertasProperty() { return jenisKertas; }
    public String getJenisKertas() { return jenisKertas.get(); }
    public void setJenisKertas(String v) { jenisKertas.set(v); }
}
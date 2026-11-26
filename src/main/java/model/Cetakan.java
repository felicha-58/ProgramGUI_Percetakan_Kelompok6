package main.java.model;

import javafx.beans.property.*;

public abstract class Cetakan {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty kode = new SimpleStringProperty();
    private final StringProperty nama = new SimpleStringProperty();
    private final StringProperty ukuran = new SimpleStringProperty();
    private final DoubleProperty hargaSatuan = new SimpleDoubleProperty();

    public Cetakan(int id, String kode, String nama, String ukuran, double hargaSatuan) {
        setId(id); setKode(kode); setNama(nama);
        setUkuran(ukuran); setHargaSatuan(hargaSatuan);
    }

    public abstract boolean validate();
    public abstract String getDetail();

    public String getTipe() {
        if (this instanceof Hangtag) return "Hangtag";
        if (this instanceof Brosur) return "Brosur";
        if (this instanceof KartuNama) return "KartuNama";
        return "Cetakan";
    }

    public double hitungHarga(int jumlah) { return jumlah * getHargaSatuan(); }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public StringProperty kodeProperty() { return kode; }
    public String getKode() { return kode.get(); }
    public void setKode(String v) { kode.set(v); }

    public StringProperty namaProperty() { return nama; }
    public String getNama() { return nama.get(); }
    public void setNama(String v) { nama.set(v); }

    public StringProperty ukuranProperty() { return ukuran; }
    public String getUkuran() { return ukuran.get(); }
    public void setUkuran(String v) { ukuran.set(v); }

    public DoubleProperty hargaSatuanProperty() { return hargaSatuan; }
    public double getHargaSatuan() { return hargaSatuan.get(); }
    public void setHargaSatuan(double v) { hargaSatuan.set(v); }

    @Override
    public String toString() { return getKode() + " - " + getNama(); }
}
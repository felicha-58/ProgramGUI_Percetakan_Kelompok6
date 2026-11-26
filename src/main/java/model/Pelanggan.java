package main.java.model;

import javafx.beans.property.*;

public class Pelanggan {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nama = new SimpleStringProperty();
    private final StringProperty alamat = new SimpleStringProperty();

    public Pelanggan(int id, String nama, String alamat) {
        setId(id); setNama(nama); setAlamat(alamat);
    }

    public boolean validate() {
        return getNama() != null && !getNama().trim().isEmpty();
    }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public StringProperty namaProperty() { return nama; }
    public String getNama() { return nama.get(); }
    public void setNama(String v) { nama.set(v); }

    public StringProperty alamatProperty() { return alamat; }
    public String getAlamat() { return alamat.get(); }
    public void setAlamat(String v) { alamat.set(v); }

    @Override
    public String toString() { return getNama(); }
}
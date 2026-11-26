package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Hangtag extends Cetakan {
    private final StringProperty bentuk = new SimpleStringProperty();
    private final StringProperty jenisTali = new SimpleStringProperty();

    public Hangtag(int id, String kode, String nama, String ukuran, double harga,
                   String bentuk, String jenisTali) {
        super(id, kode, nama, ukuran, harga);
        setBentuk(bentuk); setJenisTali(jenisTali);
    }

    @Override
    public boolean validate() {
        return getKode() != null && !getKode().trim().isEmpty()
            && getNama() != null && !getNama().trim().isEmpty()
            && getHargaSatuan() > 0;
    }

    @Override
    public String getDetail() { return "Bentuk: " + getBentuk() + ", Tali: " + getJenisTali(); }

    public StringProperty bentukProperty() { return bentuk; }
    public String getBentuk() { return bentuk.get(); }
    public void setBentuk(String v) { bentuk.set(v); }

    public StringProperty jenisTaliProperty() { return jenisTali; }
    public String getJenisTali() { return jenisTali.get(); }
    public void setJenisTali(String v) { jenisTali.set(v); }
}
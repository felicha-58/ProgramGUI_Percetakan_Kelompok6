package main.java.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KartuNama extends Cetakan {
    private final StringProperty jenisKertas = new SimpleStringProperty();
    private final StringProperty finishing = new SimpleStringProperty();

    public KartuNama(int id, String kode, String nama, String ukuran, double harga,
                     String jenisKertas, String finishing) {
        super(id, kode, nama, ukuran, harga);
        setJenisKertas(jenisKertas); setFinishing(finishing);
    }

    @Override
    public boolean validate() {
        return getKode() != null && !getKode().trim().isEmpty()
            && getNama() != null && !getNama().trim().isEmpty()
            && getHargaSatuan() > 0;
    }

    @Override
    public String getDetail() { return "Kertas: " + getJenisKertas() + ", Finishing: " + getFinishing(); }

    public StringProperty jenisKertasProperty() { return jenisKertas; }
    public String getJenisKertas() { return jenisKertas.get(); }
    public void setJenisKertas(String v) { jenisKertas.set(v); }

    public StringProperty finishingProperty() { return finishing; }
    public String getFinishing() { return finishing.get(); }
    public void setFinishing(String v) { finishing.set(v); }
}
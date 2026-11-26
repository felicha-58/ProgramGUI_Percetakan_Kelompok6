package main.java.model;

import java.time.LocalDate;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Nota {
    private int id;
    private final StringProperty nomorNota = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> tanggal = new SimpleObjectProperty<>();
    private final ObjectProperty<Pelanggan> pelanggan = new SimpleObjectProperty<>();
    private final ObservableList<NotaItem> items = FXCollections.observableArrayList();

    public Nota(int id, String nomorNota, LocalDate tanggal, Pelanggan pelanggan) {
        this.id = id;
        setNomorNota(nomorNota); setTanggal(tanggal); setPelanggan(pelanggan);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public StringProperty nomorNotaProperty() { return nomorNota; }
    public String getNomorNota() { return nomorNota.get(); }
    public void setNomorNota(String v) { nomorNota.set(v); }

    public ObjectProperty<LocalDate> tanggalProperty() { return tanggal; }
    public LocalDate getTanggal() { return tanggal.get(); }
    public void setTanggal(LocalDate v) { tanggal.set(v); }

    public ObjectProperty<Pelanggan> pelangganProperty() { return pelanggan; }
    public Pelanggan getPelanggan() { return pelanggan.get(); }
    public void setPelanggan(Pelanggan p) { pelanggan.set(p); }

    public ObservableList<NotaItem> getItems() { return items; }
    public void setItems(ObservableList<NotaItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
    }

    public double hitungDpp() {
        return items.stream().mapToDouble(NotaItem::getSubtotal).sum();
    }
    public double hitungPajak() { return hitungDpp() * 0.11; }
    public double hitungTotal() { return hitungDpp() + hitungPajak(); }
    public double getTotal() { return hitungTotal(); }
}
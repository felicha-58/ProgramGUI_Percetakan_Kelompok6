package main.java.controller;

import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import main.java.database.PelangganDAO;
import main.java.model.Pelanggan;

public class PelangganTableController {
    @FXML private TextField namaField;
    @FXML private TextField alamatField;
    @FXML private TableView<Pelanggan> pelangganTable;
    @FXML private TableColumn<Pelanggan, String> namaCol;
    @FXML private TableColumn<Pelanggan, String> alamatCol;

    private final PelangganDAO pelangganDAO = new PelangganDAO();

    @FXML
    public void initialize() {
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        alamatCol.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        refreshTable();

        pelangganTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) {
                namaField.setText(nv.getNama());
                alamatField.setText(nv.getAlamat());
            } else {
                clearForm();
            }
        });
    }

    private void refreshTable() {
        try {
            pelangganTable.setItems(FXCollections.observableArrayList(pelangganDAO.getAllPelanggan()));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private Pelanggan readForm() {
        return new Pelanggan(-1, namaField.getText(), alamatField.getText());
    }

    @FXML
    private void handleSubmit() {
        Pelanggan p = readForm();
        if (!p.validate()) { showError("Warning", "Nama tidak boleh kosong!"); return; }
        try {
            pelangganDAO.savePelanggan(p);
            refreshTable();
            clearForm();
        } catch (SQLException e) { showError("Submit Error", e.getMessage()); }
    }

    @FXML
    private void handleUpdate() {
        Pelanggan sel = pelangganTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih baris dulu."); return; }
        Pelanggan p = readForm();
        if (!p.validate()) { showError("Warning", "Nama tidak boleh kosong!"); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Yakin update?", ButtonType.YES, ButtonType.NO);
        c.showAndWait();
        if (c.getResult() != ButtonType.YES) return;
        try {
            pelangganDAO.updatePelanggan(p, sel.getId());
            refreshTable();
            clearForm();
        } catch (SQLException e) { showError("Update Error", e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        Pelanggan sel = pelangganTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih baris dulu."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Yakin hapus?", ButtonType.YES, ButtonType.NO);
        c.showAndWait();
        if (c.getResult() != ButtonType.YES) return;
        try {
            pelangganDAO.deletePelanggan(sel.getId());
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            showError("Delete Error", "Pelanggan mungkin masih dipakai di nota.\n" + e.getMessage());
        }
    }

    private void clearForm() {
        namaField.clear(); alamatField.clear();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
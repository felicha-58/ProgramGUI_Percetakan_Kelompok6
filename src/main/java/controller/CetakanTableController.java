package main.java.controller;

import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.database.CetakanDAO;
import main.java.model.*;

public class CetakanTableController {
    @FXML private TextField kodeField;
    @FXML private TextField namaField;
    @FXML private ComboBox<String> tipeComboBox;
    @FXML private TextField ukuranField;
    @FXML private TextField hargaField;
    @FXML private VBox dynamicFormContainer;
    @FXML private TableView<Cetakan> cetakanTable;
    @FXML private TableColumn<Cetakan, String> kodeCol;
    @FXML private TableColumn<Cetakan, String> namaCol;
    @FXML private TableColumn<Cetakan, String> tipeCol;
    @FXML private TableColumn<Cetakan, String> ukuranCol;
    @FXML private TableColumn<Cetakan, Number> hargaCol;
    @FXML private TableColumn<Cetakan, String> detailCol;

    private TextField bentukField;
    private TextField jenisTaliField;
    private TextField jumlahHalamanField;
    private TextField jenisKertasField;
    private TextField finishingField;

    private final CetakanDAO cetakanDAO = new CetakanDAO();

    @FXML
    public void initialize() {
        kodeCol.setCellValueFactory(new PropertyValueFactory<>("kode"));
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        ukuranCol.setCellValueFactory(new PropertyValueFactory<>("ukuran"));
        hargaCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getHargaSatuan()));
        tipeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTipe()));
        detailCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDetail()));

        hargaCol.setCellFactory(col -> new TableCell<Cetakan, Number>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("Rp%,.0f", v.doubleValue()));
            }
        });

        refreshTable();
        createExtraFields(tipeComboBox.getValue(), null);

        cetakanTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) {
                kodeField.setText(nv.getKode());
                namaField.setText(nv.getNama());
                tipeComboBox.setValue(nv.getTipe());
                ukuranField.setText(nv.getUkuran());
                hargaField.setText(String.valueOf(nv.getHargaSatuan()));
                createExtraFields(nv.getTipe(), nv);
            } else {
                clearForm();
            }
        });
    }

    @FXML
    private void handleTypeSelection() {
        createExtraFields(tipeComboBox.getValue(), null);
    }

    private void createExtraFields(String tipe, Cetakan c) {
        dynamicFormContainer.getChildren().clear();
        if ("Hangtag".equals(tipe)) {
            Label l1 = new Label("Bentuk: ");
            bentukField = new TextField();
            HBox b1 = new HBox(10, l1, bentukField);
            Label l2 = new Label("Jenis Tali: ");
            jenisTaliField = new TextField();
            HBox b2 = new HBox(10, l2, jenisTaliField);
            if (c instanceof Hangtag) {
                bentukField.setText(((Hangtag) c).getBentuk());
                jenisTaliField.setText(((Hangtag) c).getJenisTali());
            }
            dynamicFormContainer.getChildren().addAll(b1, b2);
        } else if ("Brosur".equals(tipe)) {
            Label l1 = new Label("Jumlah Halaman: ");
            jumlahHalamanField = new TextField();
            HBox b1 = new HBox(10, l1, jumlahHalamanField);
            Label l2 = new Label("Jenis Kertas: ");
            jenisKertasField = new TextField();
            HBox b2 = new HBox(10, l2, jenisKertasField);
            if (c instanceof Brosur) {
                jumlahHalamanField.setText(String.valueOf(((Brosur) c).getJumlahHalaman()));
                jenisKertasField.setText(((Brosur) c).getJenisKertas());
            }
            dynamicFormContainer.getChildren().addAll(b1, b2);
        } else if ("KartuNama".equals(tipe)) {
            Label l1 = new Label("Jenis Kertas: ");
            jenisKertasField = new TextField();
            HBox b1 = new HBox(10, l1, jenisKertasField);
            Label l2 = new Label("Finishing: ");
            finishingField = new TextField();
            HBox b2 = new HBox(10, l2, finishingField);
            if (c instanceof KartuNama) {
                jenisKertasField.setText(((KartuNama) c).getJenisKertas());
                finishingField.setText(((KartuNama) c).getFinishing());
            }
            dynamicFormContainer.getChildren().addAll(b1, b2);
        }
    }

    private Cetakan readForm() {
        String tipe = tipeComboBox.getValue();
        double harga;
        try { harga = Double.parseDouble(hargaField.getText().trim()); }
        catch (NumberFormatException e) { showError("Warning", "Harga harus angka!"); return null; }
        if ("Hangtag".equals(tipe)) {
            return new Hangtag(-1, kodeField.getText(), namaField.getText(),
                    ukuranField.getText(), harga, bentukField.getText(), jenisTaliField.getText());
        } else if ("Brosur".equals(tipe)) {
            int jh;
            try { jh = Integer.parseInt(jumlahHalamanField.getText().trim()); }
            catch (NumberFormatException e) { showError("Warning", "Jumlah halaman harus angka!"); return null; }
            return new Brosur(-1, kodeField.getText(), namaField.getText(),
                    ukuranField.getText(), harga, jh, jenisKertasField.getText());
        } else if ("KartuNama".equals(tipe)) {
            return new KartuNama(-1, kodeField.getText(), namaField.getText(),
                    ukuranField.getText(), harga, jenisKertasField.getText(), finishingField.getText());
        }
        return null;
    }

    private void refreshTable() {
        try {
            cetakanTable.setItems(FXCollections.observableArrayList(cetakanDAO.getAllCetakan()));
        } catch (SQLException e) { showError("Database Error", e.getMessage()); }
    }

    @FXML
    private void handleSubmit() {
        Cetakan c = readForm();
        if (c == null) return;
        if (!c.validate()) { showError("Warning", "Data tidak valid (kode, nama, harga wajib benar)."); return; }
        try {
            cetakanDAO.saveCetakan(c);
            refreshTable();
            clearForm();
        } catch (SQLException e) { showError("Submit Error", e.getMessage()); }
    }

    @FXML
    private void handleUpdate() {
        Cetakan sel = cetakanTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih baris dulu."); return; }
        Cetakan c = readForm();
        if (c == null || !c.validate()) return;
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Yakin update?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait();
        if (conf.getResult() != ButtonType.YES) return;
        try {
            cetakanDAO.updateCetakan(c, sel.getId());
            refreshTable();
            clearForm();
        } catch (SQLException e) { showError("Update Error", e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        Cetakan sel = cetakanTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih baris dulu."); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Yakin hapus?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait();
        if (conf.getResult() != ButtonType.YES) return;
        try {
            cetakanDAO.deleteCetakan(sel.getId());
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            showError("Delete Error", "Cetakan mungkin masih dipakai di nota.\n" + e.getMessage());
        }
    }

    private void clearForm() {
        kodeField.clear(); namaField.clear(); ukuranField.clear(); hargaField.clear();
        tipeComboBox.setValue("Hangtag");
        createExtraFields("Hangtag", null);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
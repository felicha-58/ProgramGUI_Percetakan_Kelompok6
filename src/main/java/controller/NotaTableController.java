package main.java.controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.java.database.*;
import main.java.model.*;

public class NotaTableController implements CetakNota {
    @FXML private TextField nomorField;
    @FXML private DatePicker tanggalPicker;
    @FXML private ComboBox<Pelanggan> pelangganCombo;
    @FXML private ComboBox<Cetakan> cetakanCombo;
    @FXML private TextField jumlahField;
    @FXML private TableView<NotaItem> cartTable;
    @FXML private TableColumn<NotaItem, Cetakan> cartCetakanCol;
    @FXML private TableColumn<NotaItem, Number> cartHargaCol;
    @FXML private TableColumn<NotaItem, Number> cartJumlahCol;
    @FXML private TableColumn<NotaItem, Number> cartSubtotalCol;
    @FXML private TableView<Nota> notaTable;
    @FXML private TableColumn<Nota, String> notaNomorCol;
    @FXML private TableColumn<Nota, LocalDate> notaTanggalCol;
    @FXML private TableColumn<Nota, Pelanggan> notaPelangganCol;
    @FXML private TableColumn<Nota, Number> notaTotalCol;

    private final ObservableList<NotaItem> cartItems = FXCollections.observableArrayList();
    private final NotaDAO notaDAO = new NotaDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final CetakanDAO cetakanDAO = new CetakanDAO();

    @FXML
    public void initialize() {
        cartCetakanCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCetakan()));
        cartHargaCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getCetakan().getHargaSatuan()));
        cartJumlahCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getJumlah()));
        cartSubtotalCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()));

        cartHargaCol.setCellFactory(col -> currencyCell());
        cartSubtotalCol.setCellFactory(col -> currencyCell());
        cartTable.setItems(cartItems);

        notaNomorCol.setCellValueFactory(c -> c.getValue().nomorNotaProperty());
        notaTanggalCol.setCellValueFactory(c -> c.getValue().tanggalProperty());
        notaPelangganCol.setCellValueFactory(c -> c.getValue().pelangganProperty());
        notaTotalCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));
        notaTotalCol.setCellFactory(col -> currencyCell());

        refreshCombos();
        refreshNotaTable();
        clearForm();

        notaTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> {
            if (nv != null) {
                nomorField.setText(nv.getNomorNota());
                tanggalPicker.setValue(nv.getTanggal());
                pelangganCombo.setValue(nv.getPelanggan());
                cartItems.setAll(nv.getItems());
            } else {
                clearForm();
            }
        });
    }

    
    private <T> TableCell<T, Number> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("Rp%,.0f", v.doubleValue()));
            }
        };
    }

    @FXML
    private void handleRefreshCombos() {
        refreshCombos();
    }

    private void refreshCombos() {
        try {
            pelangganCombo.setItems(FXCollections.observableArrayList(pelangganDAO.getAllPelanggan()));
            if (pelangganCombo.getValue() == null) pelangganCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) { showError("Database Error", e.getMessage()); }
        try {
            cetakanCombo.setItems(FXCollections.observableArrayList(cetakanDAO.getAllCetakan()));
            if (cetakanCombo.getValue() == null) cetakanCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) { showError("Database Error", e.getMessage()); }
    }

    private void refreshNotaTable() {
        try {
            notaTable.setItems(FXCollections.observableArrayList(notaDAO.getAllNota()));
        } catch (SQLException e) { showError("Database Error", e.getMessage()); }
    }

    @FXML
    private void handleAddItem() {
        Cetakan c = cetakanCombo.getValue();
        if (c == null) { showError("Warning", "Pilih cetakan dulu."); return; }
        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahField.getText().trim());
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Warning", "Jumlah harus angka > 0.");
            return;
        }
        cartItems.add(new NotaItem(-1, c, jumlah));
        jumlahField.clear();
    }

    @FXML
    private void handleRemoveItem() {
        NotaItem sel = cartTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Warning", "Pilih item yang akan dihapus."); return; }
        cartItems.remove(sel);
    }

    private boolean validateForm() {
        if (nomorField.getText() == null || nomorField.getText().trim().isEmpty()) {
            showError("Warning", "Nomor Nota wajib diisi."); return false;
        }
        if (tanggalPicker.getValue() == null) {
            showError("Warning", "Tanggal wajib diisi."); return false;
        }
        if (pelangganCombo.getValue() == null) {
            showError("Warning", "Pelanggan wajib dipilih."); return false;
        }
        if (cartItems.isEmpty()) {
            showError("Warning", "Nota minimal harus memiliki 1 item."); return false;
        }
        return true;
    }

    private Nota readForm() {
        Nota n = new Nota(-1, nomorField.getText(), tanggalPicker.getValue(), pelangganCombo.getValue());
        n.getItems().addAll(cartItems);
        return n;
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;
        try {
            notaDAO.saveNota(readForm());
            refreshNotaTable();
            clearForm();
        } catch (SQLException e) { showError("Submit Error", e.getMessage()); }
    }

    @FXML
    private void handleUpdate() {
        Nota sel = notaTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih nota dulu."); return; }
        if (!validateForm()) return;
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Yakin update?", ButtonType.YES, ButtonType.NO);
        c.showAndWait();
        if (c.getResult() != ButtonType.YES) return;
        try {
            notaDAO.updateNota(readForm(), sel.getId());
            refreshNotaTable();
            clearForm();
        } catch (SQLException e) { showError("Update Error", e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        Nota sel = notaTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("No Selection", "Pilih nota dulu."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION, "Yakin hapus?", ButtonType.YES, ButtonType.NO);
        c.showAndWait();
        if (c.getResult() != ButtonType.YES) return;
        try {
            notaDAO.deleteNota(sel.getId());
            refreshNotaTable();
            clearForm();
        } catch (SQLException e) { showError("Delete Error", e.getMessage()); }
    }

    @Override
    @FXML
    public void cetakNota() {
        if (cartItems.isEmpty()) {
            showError("Warning", "Cart kosong, tidak ada yang dicetak.");
            return;
        }
        if (nomorField.getText() == null || nomorField.getText().trim().isEmpty()
                || tanggalPicker.getValue() == null || pelangganCombo.getValue() == null) {
            showError("Warning", "Lengkapi data nota dulu sebelum cetak.");
            return;
        }
        WebView webView = new WebView();
        webView.getEngine().loadContent(cetakNotaHtml());
        Stage stage = new Stage();
        stage.setTitle("PT. CHIIKAWA" + " - Nota");
        stage.setScene(new Scene(webView, 850, 700));
        stage.show();
        printWebView(webView, stage);
    }

    private void printWebView(WebView webView, Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(owner)) {
            webView.getEngine().print(job);
            job.endJob();
        }
    }

    private String cetakNotaHtml() {
        try (InputStream is = getClass().getResourceAsStream("/main/resources/invoice.html")) {
            if (is == null) return "<html><body><h1>Template not found</h1></body></html>";
            String template = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            Nota n = readForm();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String date = n.getTanggal().format(fmt);

            StringBuilder rows = new StringBuilder();
            for (NotaItem item : n.getItems()) {
                rows.append("<tr>")
                    .append("<td>").append(item.getCetakan().getKode()).append(" - ")
                        .append(item.getCetakan().getNama()).append("</td>")
                    .append("<td class=\"text-right\">Rp")
                        .append(String.format("%,.0f", item.getCetakan().getHargaSatuan())).append("</td>")
                    .append("<td class=\"text-center\">").append(item.getJumlah()).append("</td>")
                    .append("<td class=\"text-right\">Rp")
                        .append(String.format("%,.0f", item.getSubtotal())).append("</td>")
                    .append("</tr>");
            }

            template = template.replace("{{invoiceNumber}}", n.getNomorNota());
            template = template.replace("{{date}}", date);
            template = template.replace("{{customerName}}", n.getPelanggan().getNama());
            template = template.replace("{{customerAddress}}",
                    n.getPelanggan().getAlamat() == null ? "" : n.getPelanggan().getAlamat());
            template = template.replace("{{itemRows}}", rows.toString());
            template = template.replace("{{dpp}}", String.format("%,.0f", n.hitungDpp()));
            template = template.replace("{{ppn}}", String.format("%,.0f", n.hitungPajak()));
            template = template.replace("{{totalAmount}}", String.format("%,.0f", n.hitungTotal()));
            return template;
        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body><h1>Error generating invoice</h1></body></html>";
        }
    }

    private void clearForm() {
        nomorField.clear();
        tanggalPicker.setValue(LocalDate.now());
        pelangganCombo.getSelectionModel().clearSelection();
        cartItems.clear();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setContentText(msg); a.showAndWait();
    }
}
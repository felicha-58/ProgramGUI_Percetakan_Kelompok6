package main.java.controller;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class DashboardController {
    @FXML private TabPane mainTabPane;

    @FXML private void showPelangganView() { openOrSelectTab("Manage Pelanggan", pelangganTab()); }
    @FXML private void showCetakanView()   { openOrSelectTab("Manage Cetakan", cetakanTab()); }
    @FXML private void showNotaView()      { openOrSelectTab("Manage Nota", notaTab()); }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Keluar dari aplikasi?");
        alert.setContentText("Yakin ingin menutup aplikasi?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }

    private void openOrSelectTab(String title, Parent content) {
        if (content == null) return;
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }
        Tab newTab = new Tab(title);
        newTab.setContent(content);
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    private Parent loadFxml(String path) {
        try {
            return new FXMLLoader(getClass().getResource(path)).load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Parent pelangganTab() { return loadFxml("/main/resources/pelanggan_table.fxml"); }
    private Parent cetakanTab()   { return loadFxml("/main/resources/cetakan_table.fxml"); }
    private Parent notaTab()      { return loadFxml("/main/resources/nota_table.fxml"); }
}
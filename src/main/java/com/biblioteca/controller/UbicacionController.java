package com.biblioteca.controller;

import com.biblioteca.dao.UbicacionDAO;
import com.biblioteca.dao.impl.UbicacionDAOImpl;
import com.biblioteca.model.Ubicacion;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;

public class UbicacionController {
    @FXML private TableView<Ubicacion> tablaUbicaciones;
    @FXML private TableColumn<Ubicacion, Integer> colId;
    @FXML private TableColumn<Ubicacion, String> colPasillo;
    @FXML private TableColumn<Ubicacion, String> colEstante;
    @FXML private TableColumn<Ubicacion, Integer> colPiso;

    private final UbicacionDAO ubicacionDAO = new UbicacionDAOImpl();

    @FXML public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUbicacion"));
        colPasillo.setCellValueFactory(new PropertyValueFactory<>("pasillo"));
        colEstante.setCellValueFactory(new PropertyValueFactory<>("estante"));
        colPiso.setCellValueFactory(new PropertyValueFactory<>("piso"));
        cargar();
    }

    private void cargar() {
        try {
            tablaUbicaciones.setItems(FXCollections.observableArrayList(ubicacionDAO.findAll()));
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML private void handleNueva() {
        Dialog<Ubicacion> dialog = new Dialog<>();
        dialog.setTitle("Nueva Ubicación");
        dialog.setHeaderText("Definir nueva estantería");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtPiso = new TextField(); txtPiso.setPromptText("Ej: 1, 2...");
        TextField txtPasillo = new TextField(); txtPasillo.setPromptText("Ej: A, B, Ciencia...");
        TextField txtEstante = new TextField(); txtEstante.setPromptText("Ej: 104, Norte...");

        grid.add(new Label("Piso:"), 0, 0); grid.add(txtPiso, 1, 0);
        grid.add(new Label("Pasillo:"), 0, 1); grid.add(txtPasillo, 1, 1);
        grid.add(new Label("Estante:"), 0, 2); grid.add(txtEstante, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                try {
                    int piso = Integer.parseInt(txtPiso.getText());
                    return new Ubicacion(txtPasillo.getText(), txtEstante.getText(), piso);
                } catch (NumberFormatException e) {
                    AlertUtils.mostrarError("Error", "El piso debe ser un número entero.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                ubicacionDAO.save(u);
                cargar();
                AlertUtils.mostrarInfo("Éxito", "Ubicación agregada correctamente.");
            } catch (SQLException ex) { AlertUtils.mostrarErrorBD(ex); }
        });
    }
}
package com.biblioteca.controller;

import com.biblioteca.dao.EditorialDAO;
import com.biblioteca.dao.impl.EditorialDAOImpl;
import com.biblioteca.model.Editorial;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;

public class EditorialController {
    @FXML private TableView<Editorial> tablaEditoriales;
    @FXML private TableColumn<Editorial, Integer> colId;
    @FXML private TableColumn<Editorial, String> colNombre;
    @FXML private TableColumn<Editorial, String> colPais;

    private final EditorialDAO editorialDAO = new EditorialDAOImpl();

    @FXML public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEditorial"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPais.setCellValueFactory(new PropertyValueFactory<>("pais"));
        cargar();
    }

    private void cargar() {
        try { tablaEditoriales.setItems(FXCollections.observableArrayList(editorialDAO.findAll())); }
        catch (SQLException e) { AlertUtils.mostrarErrorBD(e); }
    }

    @FXML private void handleNueva() {
        // Diálogo Personalizado con 2 campos
        Dialog<Editorial> dialog = new Dialog<>();
        dialog.setTitle("Nueva Editorial");
        dialog.setHeaderText("Ingresa los datos de la editorial");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField(); txtNombre.setPromptText("Nombre");
        TextField txtPais = new TextField(); txtPais.setPromptText("País");

        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("País:"), 0, 1); grid.add(txtPais, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                if(txtNombre.getText().isEmpty()) return null;
                return new Editorial(txtNombre.getText(), txtPais.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editorial -> {
            try {
                editorialDAO.save(editorial);
                cargar(); // Recargar tabla
                AlertUtils.mostrarInfo("Éxito", "Editorial agregada correctamente");
            } catch (SQLException ex) { AlertUtils.mostrarErrorBD(ex); }
        });
    }

    @FXML private void handleActualizar() {
        cargar(); // Vincula esto al botón actualizar en tu FXML
    }
}
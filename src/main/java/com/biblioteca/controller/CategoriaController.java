package com.biblioteca.controller;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.impl.CategoriaDAOImpl;
import com.biblioteca.model.Categoria;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;

public class CategoriaController {
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;

    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();

    @FXML public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        cargar();
    }

    private void cargar() {
        try { tablaCategorias.setItems(FXCollections.observableArrayList(categoriaDAO.findAll())); }
        catch (SQLException e) { AlertUtils.mostrarErrorBD(e); }
    }

    @FXML private void handleNueva() {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText("Crear categoría literaria");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField(); txtNombre.setPromptText("Ej: Ciencia Ficción");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descripción breve...");
        txtDesc.setPrefRowCount(3);

        grid.add(new Label("Nombre:"), 0, 0); grid.add(txtNombre, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1); grid.add(txtDesc, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn && !txtNombre.getText().isEmpty()) {
                Categoria c = new Categoria();
                c.setNombre(txtNombre.getText());
                c.setDescripcion(txtDesc.getText());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cat -> {
            try {
                categoriaDAO.save(cat);
                cargar();
                AlertUtils.mostrarInfo("Éxito", "Categoría creada");
            } catch (SQLException ex) { AlertUtils.mostrarErrorBD(ex); }
        });
    }

    @FXML private void handleActualizar() {
        cargar();
    }
}
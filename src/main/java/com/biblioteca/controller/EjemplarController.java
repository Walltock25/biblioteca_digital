package com.biblioteca.controller;

import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.impl.EjemplarDAOImpl;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.Libro;
import com.biblioteca.model.enums.EstadoFisico;
import com.biblioteca.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.scene.paint.Color; // Importante para el color

import java.sql.SQLException;
import java.util.List;

public class EjemplarController {

    @FXML private TableView<Ejemplar> tablaEjemplares;
    @FXML private TableColumn<Ejemplar, Integer> colId;
    @FXML private TableColumn<Ejemplar, String> colCodigo;
    @FXML private TableColumn<Ejemplar, String> colLibro;
    @FXML private TableColumn<Ejemplar, String> colEstado;
    @FXML private TableColumn<Ejemplar, Boolean> colDisponible; // Ojo aquí

    private final EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarEjemplares();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEjemplar"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoFisico"));

        // --- MAGIA VISUAL AQUÍ ---
        // Esto convierte el "true" en un texto verde "DISPONIBLE" y "false" en rojo "PRESTADO"
        colDisponible.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        colDisponible.setCellFactory(column -> new TableCell<Ejemplar, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (item) {
                        setText("DISPONIBLE");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setText("PRESTADO");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Mostrar Título del Libro
        colLibro.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getLibro() != null ? cellData.getValue().getLibro().getTitulo() : "N/A"
                ));
    }

    private void cargarEjemplares() {
        try {
            // Limpiamos y recargamos desde la BD para asegurar datos frescos
            List<Ejemplar> lista = ejemplarDAO.findAll();
            tablaEjemplares.setItems(FXCollections.observableArrayList(lista));
            tablaEjemplares.refresh(); // Forzar repintado visual
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleNuevoEjemplar() {
        Dialog<Ejemplar> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Ejemplar");
        dialog.setHeaderText("Generación Automática de Código");

        ButtonType guardarBtn = new ButtonType("Generar y Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<Libro> cmbLibro = new ComboBox<>();
        cmbLibro.setPromptText("Seleccione Libro...");
        cmbLibro.setPrefWidth(250);
        try { cmbLibro.setItems(FXCollections.observableArrayList(libroDAO.findAll())); } catch (SQLException e) {}

        // Convertidor para que el Combo muestre el título correctamente
        cmbLibro.setConverter(new StringConverter<Libro>() {
            @Override public String toString(Libro l) { return l != null ? l.getTitulo() : ""; }
            @Override public Libro fromString(String s) { return null; }
        });

        ComboBox<EstadoFisico> cmbEstado = new ComboBox<>(FXCollections.observableArrayList(EstadoFisico.values()));
        cmbEstado.setValue(EstadoFisico.BUENO); // Valor por defecto

        grid.add(new Label("Libro:"), 0, 0); grid.add(cmbLibro, 1, 0);
        grid.add(new Label("Estado:"), 0, 1); grid.add(cmbEstado, 1, 1);
        grid.add(new Label("Nota: El código se genera solo."), 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn && cmbLibro.getValue() != null) {
                try {
                    Libro libro = cmbLibro.getValue();
                    Ejemplar ej = new Ejemplar();
                    ej.setLibro(libro);

                    // Lógica de generación de código (Simplificada)
                    String codigo = generarCodigo(libro);
                    ej.setCodigoBarras(codigo);

                    ej.setEstadoFisico(cmbEstado.getValue());

                    // --- AQUÍ ESTÁ EL FIX DEL BOOLEANO ---
                    ej.setDisponible(true); // ¡Forzamos explícitamente a TRUE al nacer!

                    return ej;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevoEjemplar -> {
            try {
                ejemplarDAO.save(nuevoEjemplar);
                cargarEjemplares(); // Recarga la tabla
                AlertUtils.mostrarInfo("Éxito", "Ejemplar " + nuevoEjemplar.getCodigoBarras() + " creado.");
            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        });
    }

    // Método auxiliar para generar código
    private String generarCodigo(Libro l) throws SQLException {
        // Obtenemos cuántos hay ya para calcular el siguiente número
        long cantidad = ejemplarDAO.findByLibro(l.getIdLibro()).size();
        String inicioTitulo = l.getTitulo().substring(0, Math.min(l.getTitulo().length(), 3)).toUpperCase();
        return inicioTitulo + "-" + String.format("%03d", cantidad + 1);
    }

    @FXML
    private void handleActualizar() {
        cargarEjemplares();
        AlertUtils.mostrarInfo("Sistema", "Inventario actualizado.");
    }
}
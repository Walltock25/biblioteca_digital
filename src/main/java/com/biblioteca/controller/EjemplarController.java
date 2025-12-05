package com.biblioteca.controller;

import com.biblioteca.dao.UbicacionDAO;
import com.biblioteca.dao.impl.UbicacionDAOImpl;
import com.biblioteca.model.Ubicacion;
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
    private final UbicacionDAO ubicacionDAO = new UbicacionDAOImpl();

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
        dialog.setHeaderText("Registro de Inventario");

        ButtonType guardarBtn = new ButtonType("Generar y Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        // --- COMBO BOX LIBROS ---
        ComboBox<Libro> cmbLibro = new ComboBox<>();
        cmbLibro.setPromptText("Seleccione Libro...");
        cmbLibro.setPrefWidth(300);
        try { cmbLibro.setItems(FXCollections.observableArrayList(libroDAO.findAll())); } catch (SQLException e) {}

        cmbLibro.setConverter(new StringConverter<Libro>() {
            @Override public String toString(Libro l) { return l != null ? l.getTitulo() : ""; }
            @Override public Libro fromString(String s) { return null; }
        });

        // --- NUEVO: COMBO BOX UBICACIÓN ---
        ComboBox<Ubicacion> cmbUbicacion = new ComboBox<>();
        cmbUbicacion.setPromptText("Seleccione Ubicación...");
        cmbUbicacion.setPrefWidth(300);
        try { cmbUbicacion.setItems(FXCollections.observableArrayList(ubicacionDAO.findAll())); } catch (SQLException e) {}

        // Usamos el método getUbicacionCompleta() que ya tienes en tu modelo
        cmbUbicacion.setConverter(new StringConverter<Ubicacion>() {
            @Override public String toString(Ubicacion u) {
                return u != null ? u.getUbicacionCompleta() : "";
            }
            @Override public Ubicacion fromString(String s) { return null; }
        });

        // --- COMBO BOX ESTADO ---
        ComboBox<EstadoFisico> cmbEstado = new ComboBox<>(FXCollections.observableArrayList(EstadoFisico.values()));
        cmbEstado.setValue(EstadoFisico.BUENO);

        // Agregamos todo al Grid
        grid.add(new Label("Libro:"), 0, 0); grid.add(cmbLibro, 1, 0);
        grid.add(new Label("Ubicación:"), 0, 1); grid.add(cmbUbicacion, 1, 1); // <--- Nuevo campo
        grid.add(new Label("Estado:"), 0, 2); grid.add(cmbEstado, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn && cmbLibro.getValue() != null) {
                try {
                    Ejemplar ej = new Ejemplar();
                    ej.setLibro(cmbLibro.getValue());

                    // Asignamos la ubicación seleccionada (puede ser null y no pasa nada gracias a tu DAO fix)
                    ej.setUbicacion(cmbUbicacion.getValue());

                    ej.setEstadoFisico(cmbEstado.getValue());
                    ej.setDisponible(true);

                    // Generamos código
                    ej.setCodigoBarras(generarCodigo(cmbLibro.getValue()));

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
                cargarEjemplares();
                AlertUtils.mostrarInfo("Éxito", "Ejemplar guardado en: " +
                        (nuevoEjemplar.getUbicacion() != null ? nuevoEjemplar.getUbicacion().getUbicacionCompleta() : "Sin ubicación"));
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
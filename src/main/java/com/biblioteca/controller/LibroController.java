package com.biblioteca.controller;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.dao.EditorialDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.impl.CategoriaDAOImpl;
import com.biblioteca.dao.impl.EditorialDAOImpl;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Categoria;
import com.biblioteca.model.Editorial;
import com.biblioteca.model.Libro;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LibroController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, Integer> colId;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colEditorial;
    @FXML private TableColumn<Libro, String> colCategoria;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, Integer> colEjemplares;

    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final EditorialDAO editorialDAO = new EditorialDAOImpl();
    private final CategoriaDAO categoriaDAO = new CategoriaDAOImpl();
    private ObservableList<Libro> listaLibros;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarLibros();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idLibro"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));

        colEditorial.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEditorial() != null ? cellData.getValue().getEditorial().getNombre() : "N/A"
                ));

        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCategoria() != null ? cellData.getValue().getCategoria().getNombre() : "N/A"
                ));

        colEjemplares.setCellValueFactory(cellData -> {
            try {
                int count = libroDAO.countEjemplaresDisponibles(cellData.getValue().getIdLibro());
                return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
    }

    private void cargarLibros() {
        try {
            listaLibros = FXCollections.observableArrayList(libroDAO.findAll());
            tablaLibros.setItems(listaLibros);
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleBuscar() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) { cargarLibros(); return; }
        try {
            List<Libro> resultados = libroDAO.findByTitulo(busqueda);
            libroDAO.findByIsbn(busqueda).ifPresent(resultados::add);
            listaLibros = FXCollections.observableArrayList(resultados);
            tablaLibros.setItems(listaLibros);
        } catch (SQLException e) { AlertUtils.mostrarErrorBD(e); }
    }

    @FXML
    private void handleNuevoLibro() {
        mostrarDialogoLibro(null);
    }

    @FXML
    private void handleEditar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Selecciona un libro para editar");
            return;
        }
        mostrarDialogoLibro(libroSeleccionado);
    }

    @FXML
    private void handleEliminar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Selecciona un libro para eliminar");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar eliminación",
                "¿Estás seguro de eliminar '" + libroSeleccionado.getTitulo() + "'?");

        if (confirmar) {
            try {
                if (libroDAO.delete(libroSeleccionado.getIdLibro())) {
                    AlertUtils.mostrarInfo("Éxito", "Libro eliminado");
                    cargarLibros();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar (posiblemente tiene préstamos activos)");
                }
            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        Libro libro = tablaLibros.getSelectionModel().getSelectedItem();
        if (libro == null) return;

        AlertUtils.mostrarInfo("Detalles",
                "Título: " + libro.getTitulo() + "\n" +
                        "ISBN: " + libro.getIsbn() + "\n" +
                        "Editorial: " + libro.getEditorial().getNombre() + "\n" +
                        "Categoría: " + libro.getCategoria().getNombre());
    }

    @FXML
    private void handleActualizar() { cargarLibros(); }

    private void mostrarDialogoLibro(Libro libroExistente) {
        Dialog<Libro> dialog = new Dialog<>();
        dialog.setTitle(libroExistente == null ? "Nuevo Libro" : "Editar Libro");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField isbn = new TextField(); isbn.setPromptText("ISBN");
        TextField titulo = new TextField(); titulo.setPromptText("Título");
        TextField anio = new TextField(); anio.setPromptText("Año");
        ComboBox<Editorial> cmbEditorial = new ComboBox<>();
        ComboBox<Categoria> cmbCategoria = new ComboBox<>();

        // Configurar combos
        try {
            cmbEditorial.setItems(FXCollections.observableArrayList(editorialDAO.findAll()));
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.findAll()));
        } catch (SQLException e) { e.printStackTrace(); }

        StringConverter<Editorial> edConv = new StringConverter<>() {
            public String toString(Editorial e) { return e != null ? e.getNombre() : ""; }
            public Editorial fromString(String s) { return null; }
        };
        StringConverter<Categoria> catConv = new StringConverter<>() {
            public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            public Categoria fromString(String s) { return null; }
        };

        cmbEditorial.setConverter(edConv);
        cmbCategoria.setConverter(catConv);

        // Pre-cargar datos si es edición
        if (libroExistente != null) {
            isbn.setText(libroExistente.getIsbn());
            titulo.setText(libroExistente.getTitulo());
            anio.setText(String.valueOf(libroExistente.getAnioPublicacion()));
            // Seleccionar en combos
            for(Editorial e : cmbEditorial.getItems())
                if(e.getIdEditorial().equals(libroExistente.getEditorial().getIdEditorial())) cmbEditorial.setValue(e);
            for(Categoria c : cmbCategoria.getItems())
                if(c.getIdCategoria().equals(libroExistente.getCategoria().getIdCategoria())) cmbCategoria.setValue(c);
        }

        grid.add(new Label("ISBN:"), 0, 0); grid.add(isbn, 1, 0);
        grid.add(new Label("Título:"), 0, 1); grid.add(titulo, 1, 1);
        grid.add(new Label("Año:"), 0, 2); grid.add(anio, 1, 2);
        grid.add(new Label("Editorial:"), 0, 3); grid.add(cmbEditorial, 1, 3);
        grid.add(new Label("Categoría:"), 0, 4); grid.add(cmbCategoria, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == guardarBtn) {
                Libro l = libroExistente != null ? libroExistente : new Libro();
                l.setIsbn(isbn.getText());
                l.setTitulo(titulo.getText());
                try { l.setAnioPublicacion(Integer.parseInt(anio.getText())); } catch(Exception e) {}
                l.setEditorial(cmbEditorial.getValue());
                l.setCategoria(cmbCategoria.getValue());
                return l;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(libro -> {
            try {
                if (libroExistente == null) libroDAO.save(libro);
                else libroDAO.update(libro);
                cargarLibros();
                AlertUtils.mostrarInfo("Éxito", "Libro guardado");
            } catch (SQLException e) { AlertUtils.mostrarErrorBD(e); }
        });
    }
}
package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Editorial; // <--- Import nuevo
import com.biblioteca.model.Categoria; // <--- Import nuevo
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets; // <--- Import nuevo
import javafx.scene.layout.GridPane; // <--- Import nuevo

import java.sql.SQLException;
import java.util.List;
import java.util.Optional; // <--- Import nuevo

/**
 * Controlador para la gestión de libros
 */
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

        colEditorial.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEditorial() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEditorial().getNombre());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colCategoria.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCategoria() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCategoria().getNombre());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colEjemplares.setCellValueFactory(cellData -> {
            try {
                int count = libroDAO.countEjemplaresDisponibles(
                        cellData.getValue().getIdLibro());
                return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
    }

    private void cargarLibros() {
        try {
            List<Libro> libros = libroDAO.findAll();
            listaLibros = FXCollections.observableArrayList(libros);
            tablaLibros.setItems(listaLibros);

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleBuscar() {
        String busqueda = txtBuscar.getText().trim();

        if (busqueda.isEmpty()) {
            cargarLibros();
            return;
        }

        try {
            List<Libro> resultados = libroDAO.findByTitulo(busqueda);
            if (resultados.isEmpty()) {
                libroDAO.findByIsbn(busqueda).ifPresent(resultados::add);
            }

            listaLibros = FXCollections.observableArrayList(resultados);
            tablaLibros.setItems(listaLibros);

            if (resultados.isEmpty()) {
                AlertUtils.mostrarInfo("Búsqueda", "No se encontraron libros con ese criterio");
            }

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
    @FXML
    private void handleNuevoLibro() {
        // 1. Crear el diálogo
        Dialog<Libro> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Libro");
        dialog.setHeaderText("Ingresa los datos del nuevo libro");

        // 2. Configurar botones
        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        // 3. Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField isbn = new TextField();
        isbn.setPromptText("ISBN");
        TextField titulo = new TextField();
        titulo.setPromptText("Título");
        TextField anio = new TextField();
        anio.setPromptText("Año");
        TextField idEditorial = new TextField();
        idEditorial.setPromptText("ID Editorial (ej: 1)");
        TextField idCategoria = new TextField();
        idCategoria.setPromptText("ID Categoría (ej: 1)");

        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbn, 1, 0);
        grid.add(new Label("Título:"), 0, 1);
        grid.add(titulo, 1, 1);
        grid.add(new Label("Año:"), 0, 2);
        grid.add(anio, 1, 2);
        grid.add(new Label("ID Editorial:"), 0, 3);
        grid.add(idEditorial, 1, 3);
        grid.add(new Label("ID Categoría:"), 0, 4);
        grid.add(idCategoria, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 4. Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Libro libro = new Libro();
                    libro.setIsbn(isbn.getText());
                    libro.setTitulo(titulo.getText());
                    libro.setAnioPublicacion(Integer.parseInt(anio.getText()));

                    Editorial editorial = new Editorial();
                    editorial.setIdEditorial(Integer.parseInt(idEditorial.getText()));
                    libro.setEditorial(editorial);

                    Categoria categoria = new Categoria();
                    categoria.setIdCategoria(Integer.parseInt(idCategoria.getText()));
                    libro.setCategoria(categoria);

                    return libro;
                } catch (NumberFormatException e) {
                    AlertUtils.mostrarError("Error", "Año e IDs deben ser números");
                    return null;
                }
            }
            return null;
        });

        // 5. Procesar
        Optional<Libro> result = dialog.showAndWait();
        result.ifPresent(libro -> {
            try {
                libroDAO.save(libro);
                cargarLibros();
                AlertUtils.mostrarInfo("Éxito", "Libro guardado correctamente");
            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        });
    }
    // -------------------------------------

    @FXML
    private void handleEditar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Por favor selecciona un libro para editar");
            return;
        }
        AlertUtils.mostrarInfo("Función en desarrollo", "Próximamente editarás: " + libroSeleccionado.getTitulo());
    }

    @FXML
    private void handleEliminar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Por favor selecciona un libro para eliminar");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion("Confirmar eliminación",
                "¿Estás seguro de eliminar el libro:\n" + libroSeleccionado.getTitulo() + "?");

        if (confirmar) {
            try {
                if (libroDAO.delete(libroSeleccionado.getIdLibro())) {
                    AlertUtils.mostrarInfo("Éxito", "Libro eliminado correctamente");
                    cargarLibros();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar el libro");
                }
            } catch (SQLException e) {
                AlertUtils.mostrarError("Error", "No se puede eliminar el libro (tiene ejemplares asociados)");
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();
        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Por favor selecciona un libro");
            return;
        }

        try {
            int ejemplaresDisponibles = libroDAO.countEjemplaresDisponibles(libroSeleccionado.getIdLibro());
            String detalles = String.format("DETALLES DEL LIBRO\n\n" +
                            "Título: %s\nISBN: %s\nAño: %d\nEditorial: %s\nCategoría: %s\nEjemplares Disponibles: %d",
                    libroSeleccionado.getTitulo(), libroSeleccionado.getIsbn(), libroSeleccionado.getAnioPublicacion(),
                    libroSeleccionado.getEditorial().getNombre(), libroSeleccionado.getCategoria().getNombre(),
                    ejemplaresDisponibles);
            AlertUtils.mostrarInfo("Detalles del Libro", detalles);
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleActualizar() {
        cargarLibros();
        AlertUtils.mostrarInfo("Actualizado", "Lista de libros actualizada");
    }
}
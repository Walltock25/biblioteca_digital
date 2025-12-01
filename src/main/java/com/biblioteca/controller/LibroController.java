package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Libro;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

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

        // Para Editorial y Categoria, necesitamos acceder a sus propiedades
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

        // Columna de ejemplares disponibles
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
            // Buscar por título
            List<Libro> resultados = libroDAO.findByTitulo(busqueda);

            // Si no hay resultados, intentar buscar por ISBN
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

    @FXML
    private void handleNuevoLibro() {
        AlertUtils.mostrarInfo("Función en desarrollo",
                "La funcionalidad de agregar libros estará disponible próximamente.\n\n" +
                        "Por ahora, puedes agregar libros directamente en la base de datos.");
    }

    @FXML
    private void handleEditar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();

        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un libro para editar");
            return;
        }

        AlertUtils.mostrarInfo("Función en desarrollo",
                "La funcionalidad de edición estará disponible próximamente.");
    }

    @FXML
    private void handleEliminar() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();

        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un libro para eliminar");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Confirmar eliminación",
                "¿Estás seguro de eliminar el libro:\n" +
                        libroSeleccionado.getTitulo() + "?");

        if (confirmar) {
            try {
                boolean eliminado = libroDAO.delete(libroSeleccionado.getIdLibro());

                if (eliminado) {
                    AlertUtils.mostrarInfo("Éxito", "Libro eliminado correctamente");
                    cargarLibros();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar el libro");
                }

            } catch (SQLException e) {
                AlertUtils.mostrarError("Error",
                        "No se puede eliminar el libro porque tiene ejemplares asociados");
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        Libro libroSeleccionado = tablaLibros.getSelectionModel().getSelectedItem();

        if (libroSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un libro para ver detalles");
            return;
        }

        try {
            int ejemplaresDisponibles = libroDAO.countEjemplaresDisponibles(
                    libroSeleccionado.getIdLibro());

            String detalles = String.format(
                    "DETALLES DEL LIBRO\n\n" +
                            "Título: %s\n" +
                            "ISBN: %s\n" +
                            "Año de Publicación: %d\n" +
                            "Editorial: %s\n" +
                            "Categoría: %s\n" +
                            "Ejemplares Disponibles: %d",
                    libroSeleccionado.getTitulo(),
                    libroSeleccionado.getIsbn(),
                    libroSeleccionado.getAnioPublicacion(),
                    libroSeleccionado.getEditorial().getNombre(),
                    libroSeleccionado.getCategoria().getNombre(),
                    ejemplaresDisponibles
            );

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
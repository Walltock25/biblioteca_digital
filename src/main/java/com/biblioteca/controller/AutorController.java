package com.biblioteca.controller;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.dao.impl.AutorDAOImpl;
import com.biblioteca.model.Autor;
import com.biblioteca.util.AlertUtils;
import com.biblioteca.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador para la gestión de autores
 * Sigue principios SOLID y manejo de errores robusto
 */
public class AutorController {

    private static final Logger logger = LoggerFactory.getLogger(AutorController.class);

    @FXML private TextField txtBuscar;
    @FXML private TableView<Autor> tablaAutores;
    @FXML private TableColumn<Autor, Integer> colId;
    @FXML private TableColumn<Autor, String> colNombre;
    @FXML private TableColumn<Autor, String> colNacionalidad;

    private final AutorDAO autorDAO = new AutorDAOImpl();
    private ObservableList<Autor> listaAutores;

    @FXML
    public void initialize() {
        logger.debug("Inicializando AutorController");
        configurarTabla();
        cargarAutores();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAutor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNacionalidad.setCellValueFactory(new PropertyValueFactory<>("nacionalidad"));

        // Permitir selección múltiple
        tablaAutores.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void cargarAutores() {
        try {
            logger.info("Cargando lista de autores");
            List<Autor> autores = autorDAO.findAll();
            listaAutores = FXCollections.observableArrayList(autores);
            tablaAutores.setItems(listaAutores);
            logger.info("Se cargaron {} autores", autores.size());

        } catch (SQLException e) {
            logger.error("Error al cargar autores", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleBuscar() {
        String busqueda = txtBuscar.getText().trim();

        if (busqueda.isEmpty()) {
            cargarAutores();
            return;
        }

        try {
            logger.debug("Buscando autores con término: {}", busqueda);
            List<Autor> resultados = autorDAO.findByNombre(busqueda);
            listaAutores = FXCollections.observableArrayList(resultados);
            tablaAutores.setItems(listaAutores);

            if (resultados.isEmpty()) {
                AlertUtils.mostrarInfo("Búsqueda",
                        "No se encontraron autores con el término: " + busqueda);
            }

        } catch (SQLException e) {
            logger.error("Error al buscar autores", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleNuevoAutor() {
        logger.debug("Abriendo diálogo para nuevo autor");
        mostrarDialogoAutor(null);
    }

    @FXML
    private void handleEditar() {
        Autor autorSeleccionado = tablaAutores.getSelectionModel().getSelectedItem();

        if (autorSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un autor para editar");
            return;
        }

        logger.debug("Editando autor: {}", autorSeleccionado.getIdAutor());
        mostrarDialogoAutor(autorSeleccionado);
    }

    @FXML
    private void handleEliminar() {
        Autor autorSeleccionado = tablaAutores.getSelectionModel().getSelectedItem();

        if (autorSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un autor para eliminar");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Confirmar eliminación",
                String.format("¿Estás seguro de eliminar al autor '%s'?\n\n" +
                                "Esta acción no se puede deshacer si el autor no tiene libros asociados.",
                        autorSeleccionado.getNombre())
        );

        if (confirmar) {
            try {
                logger.info("Eliminando autor ID: {}", autorSeleccionado.getIdAutor());

                if (autorDAO.delete(autorSeleccionado.getIdAutor())) {
                    AlertUtils.mostrarInfo("Éxito", "Autor eliminado correctamente");
                    cargarAutores();
                } else {
                    AlertUtils.mostrarError("Error", "No se pudo eliminar el autor");
                }

            } catch (SQLException e) {
                logger.error("Error al eliminar autor", e);

                if (e.getMessage().contains("libros asociados")) {
                    AlertUtils.mostrarAdvertencia("No se puede eliminar",
                            "El autor tiene libros asociados. " +
                                    "Debes eliminar o reasignar esos libros primero.");
                } else {
                    AlertUtils.mostrarErrorBD(e);
                }
            }
        }
    }

    @FXML
    private void handleVerLibros() {
        Autor autorSeleccionado = tablaAutores.getSelectionModel().getSelectedItem();

        if (autorSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un autor para ver sus libros");
            return;
        }

        try {
            boolean tieneLibros = autorDAO.tieneLibrosAsociados(
                    autorSeleccionado.getIdAutor());

            if (tieneLibros) {
                AlertUtils.mostrarInfo("Libros del Autor",
                        String.format("El autor '%s' tiene libros registrados.\n\n" +
                                        "Ve a la sección de Libros para ver el detalle.",
                                autorSeleccionado.getNombre()));
            } else {
                AlertUtils.mostrarInfo("Sin Libros",
                        String.format("El autor '%s' no tiene libros registrados aún.",
                                autorSeleccionado.getNombre()));
            }

        } catch (SQLException e) {
            logger.error("Error al verificar libros del autor", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleActualizar() {
        logger.debug("Actualizando lista de autores");
        cargarAutores();
        txtBuscar.clear();
        AlertUtils.mostrarInfo("Actualizado", "Lista de autores actualizada");
    }

    /**
     * Muestra el diálogo para crear o editar un autor
     */
    private void mostrarDialogoAutor(Autor autorExistente) {
        Dialog<Autor> dialog = new Dialog<>();
        dialog.setTitle(autorExistente == null ? "Nuevo Autor" : "Editar Autor");
        dialog.setHeaderText(autorExistente == null ?
                "Registrar nuevo autor" :
                "Modificar información del autor");

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo del autor");
        txtNombre.setPrefWidth(300);

        TextField txtNacionalidad = new TextField();
        txtNacionalidad.setPromptText("País de origen");
        txtNacionalidad.setPrefWidth(300);

        // Pre-cargar datos si es edición
        if (autorExistente != null) {
            txtNombre.setText(autorExistente.getNombre());
            txtNacionalidad.setText(autorExistente.getNacionalidad());
        }

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Nacionalidad:"), 0, 1);
        grid.add(txtNacionalidad, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Enfocar el campo nombre
        javafx.application.Platform.runLater(txtNombre::requestFocus);

        // Validar antes de cerrar
        Button btnGuardar = (Button) dialog.getDialogPane().lookupButton(guardarBtn);
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validarCampos(txtNombre.getText(), txtNacionalidad.getText())) {
                event.consume(); // Prevenir que se cierre el diálogo
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarBtn) {
                Autor autor = autorExistente != null ? autorExistente : new Autor();
                autor.setNombre(txtNombre.getText().trim());
                autor.setNacionalidad(txtNacionalidad.getText().trim());
                return autor;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(autor -> {
            try {
                if (autorExistente == null) {
                    // Crear nuevo
                    Integer id = autorDAO.save(autor);
                    logger.info("Autor creado con ID: {}", id);
                    AlertUtils.mostrarInfo("Éxito",
                            "Autor '" + autor.getNombre() + "' registrado correctamente");
                } else {
                    // Actualizar existente
                    autorDAO.update(autor);
                    logger.info("Autor actualizado: {}", autor.getIdAutor());
                    AlertUtils.mostrarInfo("Éxito",
                            "Autor actualizado correctamente");
                }

                cargarAutores();

            } catch (SQLException e) {
                logger.error("Error al guardar autor", e);
                AlertUtils.mostrarErrorBD(e);
            }
        });
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validarCampos(String nombre, String nacionalidad) {
        if (!ValidationUtils.esTextoValido(nombre, 2, 100)) {
            AlertUtils.mostrarAdvertencia("Campo inválido",
                    "El nombre del autor debe tener entre 2 y 100 caracteres");
            return false;
        }

        if (!ValidationUtils.esTextoValido(nacionalidad, 2, 50)) {
            AlertUtils.mostrarAdvertencia("Campo inválido",
                    "La nacionalidad debe tener entre 2 y 50 caracteres");
            return false;
        }

        return true;
    }
}
package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.ReservaDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.dao.impl.ReservaDAOImpl;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la gestión de reservas de libros
 */
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);
    private static final int MAX_RESERVAS_SIMULTANEAS = 5;

    @FXML private ComboBox<Usuario> cmbUsuario;
    @FXML private ComboBox<Libro> cmbLibro;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, Integer> colId;
    @FXML private TableColumn<Reserva, String> colUsuario;
    @FXML private TableColumn<Reserva, String> colLibro;
    @FXML private TableColumn<Reserva, String> colFecha;
    @FXML private TableColumn<Reserva, String> colEstado;

    private final ReservaDAO reservaDAO = new ReservaDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    private ObservableList<Reserva> listaReservas;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        logger.debug("Inicializando ReservaController");
        configurarTabla();
        configurarCombos();
        cargarDatos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idReserva"));

        colUsuario.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUsuario() != null) {
                return new SimpleStringProperty(cellData.getValue().getUsuario().getNombreCompleto());
            }
            return new SimpleStringProperty("N/A");
        });

        colLibro.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLibro() != null) {
                return new SimpleStringProperty(cellData.getValue().getLibro().getTitulo());
            }
            return new SimpleStringProperty("N/A");
        });

        colFecha.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaReserva().format(formatter)));

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado().getDescripcion()));
    }

    private void configurarCombos() {
        // Filtro de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todas", "Pendiente", "Notificado", "Cancelado", "Completado"));
        cmbFiltroEstado.getSelectionModel().selectFirst();

        // Combo de usuarios
        cmbUsuario.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario u) {
                return u != null ? u.getNombreCompleto() + " (" + u.getEmail() + ")" : "";
            }
            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });

        // Combo de libros
        cmbLibro.setConverter(new StringConverter<Libro>() {
            @Override
            public String toString(Libro l) {
                return l != null ? l.getTitulo() + " - " + l.getIsbn() : "";
            }
            @Override
            public Libro fromString(String string) {
                return null;
            }
        });
    }

    private void cargarDatos() {
        try {
            // Cargar reservas
            List<Reserva> reservas = reservaDAO.findAll();
            listaReservas = FXCollections.observableArrayList(reservas);
            tablaReservas.setItems(listaReservas);

            // Cargar usuarios
            cmbUsuario.setItems(FXCollections.observableArrayList(usuarioDAO.findAll()));

            // Cargar libros
            cmbLibro.setItems(FXCollections.observableArrayList(libroDAO.findAll()));

            logger.info("Datos cargados: {} reservas", reservas.size());

        } catch (SQLException e) {
            logger.error("Error al cargar datos", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleRealizarReserva() {
        Usuario usuario = cmbUsuario.getValue();
        Libro libro = cmbLibro.getValue();

        if (usuario == null || libro == null) {
            AlertUtils.mostrarAdvertencia("Datos incompletos",
                    "Por favor selecciona un usuario y un libro");
            return;
        }

        try {
            logger.debug("Intentando crear reserva para usuario {} y libro {}",
                    usuario.getIdUsuario(), libro.getIdLibro());

            // Validar que no tenga una reserva activa del mismo libro
            if (reservaDAO.usuarioTieneReservaActiva(usuario.getIdUsuario(), libro.getIdLibro())) {
                AlertUtils.mostrarAdvertencia("Reserva duplicada",
                        String.format("El usuario ya tiene una reserva activa para '%s'", libro.getTitulo()));
                return;
            }

            // Validar límite de reservas simultáneas
            int reservasActivas = reservaDAO.countReservasActivasByUsuario(usuario.getIdUsuario());
            if (reservasActivas >= MAX_RESERVAS_SIMULTANEAS) {
                AlertUtils.mostrarAdvertencia("Límite alcanzado",
                        String.format("El usuario ha alcanzado el límite de %d reservas simultáneas",
                                MAX_RESERVAS_SIMULTANEAS));
                return;
            }

            // Crear la reserva
            Reserva reserva = new Reserva(usuario, libro);
            Integer idReserva = reservaDAO.save(reserva);

            logger.info("Reserva creada exitosamente con ID: {}", idReserva);

            AlertUtils.mostrarInfo("Reserva Exitosa",
                    String.format("Reserva registrada para '%s'\n\n" +
                                    "Se notificará al usuario cuando el libro esté disponible.",
                            libro.getTitulo()));

            // Limpiar y recargar
            cmbUsuario.getSelectionModel().clearSelection();
            cmbLibro.getSelectionModel().clearSelection();
            cargarDatos();

        } catch (SQLException e) {
            logger.error("Error al realizar reserva", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleCancelar() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona una reserva para cancelar");
            return;
        }

        if (seleccionada.getEstado() != EstadoReserva.PENDIENTE) {
            AlertUtils.mostrarInfo("Estado inválido",
                    "Solo se pueden cancelar reservas en estado Pendiente");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion("Cancelar Reserva",
                String.format("¿Confirmas la cancelación de la reserva de '%s' para %s?",
                        seleccionada.getLibro().getTitulo(),
                        seleccionada.getUsuario().getNombreCompleto()));

        if (confirmar) {
            try {
                seleccionada.setEstado(EstadoReserva.CANCELADO);
                reservaDAO.update(seleccionada);

                logger.info("Reserva {} cancelada", seleccionada.getIdReserva());
                AlertUtils.mostrarInfo("Éxito", "Reserva cancelada correctamente");
                cargarDatos();

            } catch (SQLException e) {
                logger.error("Error al cancelar reserva", e);
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleCompletar() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona una reserva para completar");
            return;
        }

        if (seleccionada.getEstado() != EstadoReserva.NOTIFICADO) {
            AlertUtils.mostrarInfo("Estado inválido",
                    "Solo se pueden completar reservas en estado Notificado");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion("Completar Reserva",
                String.format("¿El usuario %s recogió el libro '%s'?",
                        seleccionada.getUsuario().getNombreCompleto(),
                        seleccionada.getLibro().getTitulo()));

        if (confirmar) {
            try {
                seleccionada.setEstado(EstadoReserva.COMPLETADO);
                reservaDAO.update(seleccionada);

                logger.info("Reserva {} completada", seleccionada.getIdReserva());
                AlertUtils.mostrarInfo("Éxito",
                        "Reserva completada. Recuerda registrar el préstamo en el módulo correspondiente.");
                cargarDatos();

            } catch (SQLException e) {
                logger.error("Error al completar reserva", e);
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleNotificar() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona una reserva para notificar");
            return;
        }

        if (seleccionada.getEstado() != EstadoReserva.PENDIENTE) {
            AlertUtils.mostrarInfo("Estado inválido",
                    "Solo se pueden notificar reservas en estado Pendiente");
            return;
        }

        try {
            seleccionada.setEstado(EstadoReserva.NOTIFICADO);
            reservaDAO.update(seleccionada);

            logger.info("Reserva {} notificada", seleccionada.getIdReserva());
            AlertUtils.mostrarInfo("Usuario Notificado",
                    String.format("Se ha notificado a %s que el libro '%s' está disponible.\n\n" +
                                    "Email: %s",
                            seleccionada.getUsuario().getNombreCompleto(),
                            seleccionada.getLibro().getTitulo(),
                            seleccionada.getUsuario().getEmail()));
            cargarDatos();

        } catch (SQLException e) {
            logger.error("Error al notificar reserva", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleFiltrar() {
        String estadoStr = cmbFiltroEstado.getValue();

        if (estadoStr == null || estadoStr.equals("Todas")) {
            cargarDatos();
            return;
        }

        try {
            EstadoReserva estado = EstadoReserva.fromString(estadoStr);
            List<Reserva> filtradas = reservaDAO.findAll().stream()
                    .filter(r -> r.getEstado() == estado)
                    .toList();

            tablaReservas.setItems(FXCollections.observableArrayList(filtradas));

        } catch (SQLException e) {
            logger.error("Error al filtrar reservas", e);
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleVerDetalles() {
        Reserva r = tablaReservas.getSelectionModel().getSelectedItem();

        if (r == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona una reserva");
            return;
        }

        String detalles = String.format(
                "DETALLES DE LA RESERVA\n\n" +
                        "ID: %d\n" +
                        "Usuario: %s\n" +
                        "Email: %s\n" +
                        "Libro: %s\n" +
                        "ISBN: %s\n" +
                        "Fecha de Reserva: %s\n" +
                        "Estado: %s",
                r.getIdReserva(),
                r.getUsuario().getNombreCompleto(),
                r.getUsuario().getEmail(),
                r.getLibro().getTitulo(),
                r.getLibro().getIsbn(),
                r.getFechaReserva().format(formatter),
                r.getEstado().getDescripcion()
        );

        AlertUtils.mostrarInfo("Detalles de la Reserva", detalles);
    }

    @FXML
    private void handleActualizar() {
        logger.debug("Actualizando lista de reservas");
        cargarDatos();
        AlertUtils.mostrarInfo("Actualizado", "Lista de reservas actualizada");
    }
}
package com.biblioteca.controller;

import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.EjemplarDAOImpl;
import com.biblioteca.dao.impl.PrestamoDAOImpl;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.util.AlertUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PrestamoController {

    // Cambiamos TextField por ComboBox para facilitar la selección
    @FXML private ComboBox<Usuario> cmbUsuario;
    @FXML private ComboBox<Ejemplar> cmbEjemplar;
    @FXML private TextField txtDias;
    @FXML private ComboBox<String> cmbFiltroEstado; // Renombrado para evitar confusión
    @FXML private TableView<Prestamo> tablaPrestamos;

    // Columnas
    @FXML private TableColumn<Prestamo, Integer> colId;
    @FXML private TableColumn<Prestamo, String> colUsuario;
    @FXML private TableColumn<Prestamo, String> colLibro;
    @FXML private TableColumn<Prestamo, String> colCodigoBarras;
    @FXML private TableColumn<Prestamo, String> colFechaSalida;
    @FXML private TableColumn<Prestamo, String> colFechaEsperada;
    @FXML private TableColumn<Prestamo, String> colEstado;

    private final PrestamoService prestamoService = new PrestamoService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();

    private ObservableList<Prestamo> listaPrestamos;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCombos();
        cargarDatos();
    }

    private void configuringTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));

        colUsuario.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUsuario() != null) {
                return new SimpleStringProperty(cellData.getValue().getUsuario().getNombreCompleto());
            }
            return new SimpleStringProperty("N/A");
        });

        colLibro.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEjemplar() != null && cellData.getValue().getEjemplar().getLibro() != null) {
                return new SimpleStringProperty(cellData.getValue().getEjemplar().getLibro().getTitulo());
            }
            return new SimpleStringProperty("N/A");
        });

        colCodigoBarras.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEjemplar() != null) {
                return new SimpleStringProperty(cellData.getValue().getEjemplar().getCodigoBarras());
            }
            return new SimpleStringProperty("N/A");
        });

        colFechaSalida.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaSalida().format(formatter)));

        colFechaEsperada.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFechaDevolucionEsperada().format(formatter)));

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado().getDescripcion()));
    }

    private void configurarTabla() {
        // Redirige al método corregido arriba (typo fix)
        configuringTabla();
    }

    private void configurarCombos() {
        // Configurar Filtro de Estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Activo", "Finalizado", "Atrasado"));
        cmbFiltroEstado.getSelectionModel().selectFirst();

        // Configurar visualización de Usuario en el Combo
        cmbUsuario.setConverter(new StringConverter<Usuario>() {
            @Override public String toString(Usuario u) { return u != null ? u.getNombreCompleto() : ""; }
            @Override public Usuario fromString(String string) { return null; }
        });

        // Configurar visualización de Ejemplar en el Combo
        cmbEjemplar.setConverter(new StringConverter<Ejemplar>() {
            @Override
            public String toString(Ejemplar e) {
                if (e == null) return "";
                return e.getCodigoBarras() + " - " + e.getLibro().getTitulo();
            }
            @Override public Ejemplar fromString(String string) { return null; }
        });
    }

    private void cargarDatos() {
        try {
            // 1. Cargar Préstamos Activos
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosActivosDeUsuario(null);
            listaPrestamos = FXCollections.observableArrayList(prestamos);
            tablaPrestamos.setItems(listaPrestamos);

            // 2. Cargar Usuarios para el combo
            cmbUsuario.setItems(FXCollections.observableArrayList(usuarioDAO.findAll()));

            // 3. Cargar Ejemplares DISPONIBLES para el combo
            // Filtramos solo los que están disponibles para no prestar algo que ya está prestado
            List<Ejemplar> disponibles = ejemplarDAO.findAll().stream()
                    .filter(Ejemplar::getDisponible)
                    .collect(Collectors.toList());
            cmbEjemplar.setItems(FXCollections.observableArrayList(disponibles));

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleRealizarPrestamo() {
        Usuario usuario = cmbUsuario.getValue();
        Ejemplar ejemplar = cmbEjemplar.getValue();

        if (usuario == null || ejemplar == null) {
            AlertUtils.mostrarAdvertencia("Datos incompletos", "Selecciona un usuario y un ejemplar.");
            return;
        }

        try {
            // Realizar préstamo
            Prestamo prestamo = prestamoService.prestarLibro(usuario.getIdUsuario(), ejemplar.getIdEjemplar());

            AlertUtils.mostrarInfo("Préstamo Exitoso",
                    "Devolución esperada: " + prestamo.getFechaDevolucionEsperada().format(formatter));

            // Limpiar y recargar TODO (para que el ejemplar desaparezca de disponibles)
            cmbUsuario.getSelectionModel().clearSelection();
            cmbEjemplar.getSelectionModel().clearSelection();
            cargarDatos();

        } catch (IllegalStateException e) {
            AlertUtils.mostrarAdvertencia("No permitido", e.getMessage());
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleDevolver() {
        Prestamo seleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección", "Selecciona un préstamo para devolver.");
            return;
        }

        if (seleccionado.getEstado() == EstadoPrestamo.FINALIZADO) {
            AlertUtils.mostrarInfo("Info", "Este préstamo ya fue devuelto.");
            return;
        }

        if (AlertUtils.mostrarConfirmacion("Devolución", "¿Confirmas la recepción del libro?")) {
            try {
                prestamoService.devolverLibro(seleccionado.getIdPrestamo());

                // Verificar multas
                if (seleccionado.estaAtrasado()) {
                    long dias = seleccionado.calcularDiasRetraso();
                    AlertUtils.mostrarAdvertencia("Retraso Detectado",
                            "El libro tiene " + dias + " días de retraso. Se ha generado una multa.");
                } else {
                    AlertUtils.mostrarInfo("Éxito", "Libro devuelto correctamente.");
                }

                cargarDatos(); // Recargar tablas y combos (el libro vuelve a estar disponible)

            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleFiltrar() {
        String estadoStr = cmbFiltroEstado.getValue();
        if (estadoStr == null || estadoStr.equals("Todos")) {
            cargarDatos();
            return;
        }
        try {
            EstadoPrestamo estado = EstadoPrestamo.fromString(estadoStr);
            List<Prestamo> filtrados = prestamoService.obtenerPrestamosAtrasados(); // O usar un DAO específico
            if (estado == EstadoPrestamo.ACTIVO) {
                filtrados = prestamoService.obtenerPrestamosActivosDeUsuario(null);
            }
            tablaPrestamos.setItems(FXCollections.observableArrayList(filtrados));
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleActualizar() {
        cargarDatos();
        AlertUtils.mostrarInfo("Actualizado", "Datos recargados correctamente");
    }

    @FXML private void handleVerDetalles() {
        // Misma lógica de antes...
        Prestamo p = tablaPrestamos.getSelectionModel().getSelectedItem();
        if(p!=null) AlertUtils.mostrarInfo("Detalles", p.toString());
    }
}
package com.biblioteca.controller;

import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la gestión de préstamos
 */
public class PrestamoController {

    @FXML private TextField txtIdUsuario;
    @FXML private TextField txtIdEjemplar;
    @FXML private TextField txtDias;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TableView<Prestamo> tablaPrestamos;
    @FXML private TableColumn<Prestamo, Integer> colId;
    @FXML private TableColumn<Prestamo, String> colUsuario;
    @FXML private TableColumn<Prestamo, String> colLibro;
    @FXML private TableColumn<Prestamo, String> colCodigoBarras;
    @FXML private TableColumn<Prestamo, String> colFechaSalida;
    @FXML private TableColumn<Prestamo, String> colFechaEsperada;
    @FXML private TableColumn<Prestamo, String> colEstado;

    private final PrestamoService prestamoService = new PrestamoService();
    private ObservableList<Prestamo> listaPrestamos;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboEstado();
        cargarPrestamos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));

        colUsuario.setCellValueFactory(cellData -> {
            if (cellData.getValue().getUsuario() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getUsuario().getNombreCompleto());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colLibro.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEjemplar() != null &&
                    cellData.getValue().getEjemplar().getLibro() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEjemplar().getLibro().getTitulo());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colCodigoBarras.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEjemplar() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEjemplar().getCodigoBarras());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colFechaSalida.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFechaSalida().format(formatter)));

        colFechaEsperada.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFechaDevolucionEsperada().format(formatter)));

        colEstado.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEstado().getDescripcion()));
    }

    private void configurarComboEstado() {
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Activo", "Finalizado", "Atrasado"));
        cmbEstado.getSelectionModel().selectFirst();
    }

    private void cargarPrestamos() {
        try {
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosActivosDeUsuario(null);

            // Si no hay método para obtener todos, usar otro enfoque
            // Por ahora mostraremos todos los activos
            listaPrestamos = FXCollections.observableArrayList(prestamos);
            tablaPrestamos.setItems(listaPrestamos);

        } catch (Exception e) {
            AlertUtils.mostrarError("Error", "No se pudieron cargar los préstamos: " + e.getMessage());
        }
    }

    @FXML
    private void handleRealizarPrestamo() {
        try {
            // Validar campos
            if (txtIdUsuario.getText().trim().isEmpty() ||
                    txtIdEjemplar.getText().trim().isEmpty()) {
                AlertUtils.mostrarAdvertencia("Campos vacíos",
                        "Por favor completa todos los campos");
                return;
            }

            Integer idUsuario = Integer.parseInt(txtIdUsuario.getText().trim());
            Integer idEjemplar = Integer.parseInt(txtIdEjemplar.getText().trim());

            // Realizar el préstamo usando el servicio
            Prestamo prestamo = prestamoService.prestarLibro(idUsuario, idEjemplar);

            AlertUtils.mostrarInfo("Préstamo Exitoso",
                    "Préstamo registrado correctamente\n" +
                            "Fecha de devolución: " + prestamo.getFechaDevolucionEsperada().format(formatter));

            // Limpiar campos
            txtIdUsuario.clear();
            txtIdEjemplar.clear();
            txtDias.setText("14");

            // Recargar tabla
            cargarPrestamos();

        } catch (NumberFormatException e) {
            AlertUtils.mostrarError("Error de formato",
                    "Los IDs deben ser números enteros");
        } catch (IllegalStateException e) {
            AlertUtils.mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleDevolver() {
        Prestamo prestamoSeleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un préstamo para devolver");
            return;
        }

        if (prestamoSeleccionado.getEstado() == EstadoPrestamo.FINALIZADO) {
            AlertUtils.mostrarAdvertencia("Préstamo ya finalizado",
                    "Este préstamo ya fue devuelto anteriormente");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Confirmar devolución",
                "¿Confirmas la devolución del libro?");

        if (confirmar) {
            try {
                boolean exito = prestamoService.devolverLibro(
                        prestamoSeleccionado.getIdPrestamo());

                if (exito) {
                    // Verificar si hay multa
                    if (prestamoSeleccionado.estaAtrasado()) {
                        long diasRetraso = prestamoSeleccionado.calcularDiasRetraso();
                        double multa = diasRetraso * 5.0; // $5 por día

                        AlertUtils.mostrarAdvertencia("Devolución con retraso",
                                String.format("Libro devuelto con %d días de retraso.\n" +
                                        "Multa generada: $%.2f", diasRetraso, multa));
                    } else {
                        AlertUtils.mostrarInfo("Éxito", "Libro devuelto correctamente");
                    }

                    cargarPrestamos();
                }

            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        Prestamo prestamoSeleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamoSeleccionado == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Por favor selecciona un préstamo");
            return;
        }

        String detalles = String.format(
                "DETALLES DEL PRÉSTAMO\n\n" +
                        "ID: %d\n" +
                        "Usuario: %s\n" +
                        "Libro: %s\n" +
                        "Código de Barras: %s\n" +
                        "Fecha de Salida: %s\n" +
                        "Fecha Esperada: %s\n" +
                        "Estado: %s\n" +
                        "Días de retraso: %d",
                prestamoSeleccionado.getIdPrestamo(),
                prestamoSeleccionado.getUsuario().getNombreCompleto(),
                prestamoSeleccionado.getEjemplar().getLibro().getTitulo(),
                prestamoSeleccionado.getEjemplar().getCodigoBarras(),
                prestamoSeleccionado.getFechaSalida().format(formatter),
                prestamoSeleccionado.getFechaDevolucionEsperada().format(formatter),
                prestamoSeleccionado.getEstado().getDescripcion(),
                prestamoSeleccionado.calcularDiasRetraso()
        );

        AlertUtils.mostrarInfo("Detalles del Préstamo", detalles);
    }

    @FXML
    private void handleFiltrar() {
        String estadoSeleccionado = cmbEstado.getValue();

        if (estadoSeleccionado == null || estadoSeleccionado.equals("Todos")) {
            cargarPrestamos();
            return;
        }

        try {
            EstadoPrestamo estado = EstadoPrestamo.fromString(estadoSeleccionado);
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosAtrasados();

            listaPrestamos = FXCollections.observableArrayList(prestamos);
            tablaPrestamos.setItems(listaPrestamos);

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleActualizar() {
        cargarPrestamos();
        AlertUtils.mostrarInfo("Actualizado", "Lista de préstamos actualizada");
    }
}
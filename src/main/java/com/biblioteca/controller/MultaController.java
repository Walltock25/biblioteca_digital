package com.biblioteca.controller;

import com.biblioteca.dao.MultaDAO;
import com.biblioteca.dao.impl.MultaDAOImpl;
import com.biblioteca.model.Multa;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MultaController {

    @FXML private ComboBox<String> cmbEstado;
    @FXML private TableView<Multa> tablaMultas;
    @FXML private TableColumn<Multa, Integer> colId;
    @FXML private TableColumn<Multa, String> colUsuario;
    @FXML private TableColumn<Multa, BigDecimal> colMonto;
    @FXML private TableColumn<Multa, String> colMotivo;
    @FXML private TableColumn<Multa, String> colFecha;
    @FXML private TableColumn<Multa, String> colEstado;

    private final MultaDAO multaDAO = new MultaDAOImpl();
    private ObservableList<Multa> listaMultas;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurarTabla();
        configurarComboEstado();
        cargarMultas();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMulta"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        colUsuario.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty("Usuario (Préstamo ID: " +
                        cellData.getValue().getPrestamo().getIdPrestamo() + ")"));

        colFecha.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFechaGeneracion().format(formatter)));

        colEstado.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEstadoPago().getDescripcion()));
    }

    private void configurarComboEstado() {
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Pendiente", "Pagado"));
        cmbEstado.getSelectionModel().selectFirst();
    }

    private void cargarMultas() {
        try {
            List<Multa> multas = multaDAO.findAll();
            listaMultas = FXCollections.observableArrayList(multas);
            tablaMultas.setItems(listaMultas);
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleFiltrar() {
        String estadoSeleccionado = cmbEstado.getValue();

        if (estadoSeleccionado == null || estadoSeleccionado.equals("Todos")) {
            cargarMultas();
            return;
        }

        try {
            List<Multa> multas = multaDAO.findAll().stream()
                    .filter(m -> m.getEstadoPago().getDescripcion().equals(estadoSeleccionado))
                    .toList();

            listaMultas = FXCollections.observableArrayList(multas);
            tablaMultas.setItems(listaMultas);
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleMarcarPagada() {
        Multa multaSeleccionada = tablaMultas.getSelectionModel().getSelectedItem();

        if (multaSeleccionada == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Selecciona una multa primero");
            return;
        }

        if (!multaSeleccionada.estaPendiente()) {
            AlertUtils.mostrarInfo("Multa ya pagada",
                    "Esta multa ya fue marcada como pagada");
            return;
        }

        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Confirmar pago",
                String.format("¿Confirmas el pago de la multa por $%.2f?",
                        multaSeleccionada.getMonto()));

        if (confirmar) {
            try {
                multaSeleccionada.marcarComoPagada();
                boolean actualizado = multaDAO.update(multaSeleccionada);

                if (actualizado) {
                    AlertUtils.mostrarInfo("Éxito", "Multa marcada como pagada");
                    cargarMultas();
                }
            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        }
    }

    @FXML
    private void handleVerDetalles() {
        Multa multaSeleccionada = tablaMultas.getSelectionModel().getSelectedItem();

        if (multaSeleccionada == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Selecciona una multa primero");
            return;
        }

        String detalles = String.format(
                "DETALLES DE LA MULTA\n\n" +
                        "ID: %d\n" +
                        "Préstamo ID: %d\n" +
                        "Monto: $%.2f\n" +
                        "Motivo: %s\n" +
                        "Fecha de Generación: %s\n" +
                        "Estado: %s",
                multaSeleccionada.getIdMulta(),
                multaSeleccionada.getPrestamo().getIdPrestamo(),
                multaSeleccionada.getMonto(),
                multaSeleccionada.getMotivo(),
                multaSeleccionada.getFechaGeneracion().format(formatter),
                multaSeleccionada.getEstadoPago().getDescripcion()
        );

        AlertUtils.mostrarInfo("Detalles de la Multa", detalles);
    }

    @FXML
    private void handleActualizar() {
        cargarMultas();
        AlertUtils.mostrarInfo("Actualizado", "Lista de multas actualizada");
    }
}
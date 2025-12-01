package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class UsuarioController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colApellido;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colTelefono;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, LocalDate> colFechaRegistro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private ObservableList<Usuario> listaUsuarios;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarUsuarios();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colFechaRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        colRol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRol() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRol().getNombreRol());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.findAll();
            listaUsuarios = FXCollections.observableArrayList(usuarios);
            tablaUsuarios.setItems(listaUsuarios);
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleBuscar() {
        String busqueda = txtBuscar.getText().trim();

        if (busqueda.isEmpty()) {
            cargarUsuarios();
            return;
        }

        try {
            usuarioDAO.findByEmail(busqueda).ifPresentOrElse(
                    usuario -> {
                        listaUsuarios = FXCollections.observableArrayList(usuario);
                        tablaUsuarios.setItems(listaUsuarios);
                    },
                    () -> AlertUtils.mostrarInfo("Búsqueda", "No se encontró el usuario")
            );
        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void handleVerPrestamos() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Selecciona un usuario primero");
            return;
        }

        AlertUtils.mostrarInfo("Función en desarrollo",
                "Verás los préstamos de: " + usuario.getNombreCompleto());
    }

    @FXML
    private void handleVerMultas() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            AlertUtils.mostrarAdvertencia("Sin selección",
                    "Selecciona un usuario primero");
            return;
        }

        AlertUtils.mostrarInfo("Función en desarrollo",
                "Verás las multas de: " + usuario.getNombreCompleto());
    }

    @FXML
    private void handleActualizar() {
        cargarUsuarios();
        AlertUtils.mostrarInfo("Actualizado", "Lista de usuarios actualizada");
    }
}
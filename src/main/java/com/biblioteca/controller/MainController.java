package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.controller.LoginController;
import com.biblioteca.dao.*;
import com.biblioteca.dao.impl.*;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador principal del dashboard
 */
public class MainController {

    @FXML private Label lblUsuario;
    @FXML private Label lblTotalLibros;
    @FXML private Label lblPrestamosActivos;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblMultasPendientes;
    @FXML private Label lblStatus;
    @FXML private StackPane contentArea;

    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final PrestamoDAO prestamoDAO = new PrestamoDAOImpl();
    private final MultaDAO multaDAO = new MultaDAOImpl();

    @FXML
    public void initialize() {
        // Mostrar nombre del usuario actual
        if (LoginController.getUsuarioActual() != null) {
            lblUsuario.setText("Usuario: " + LoginController.getUsuarioActual().getNombreCompleto());
        }

        // Cargar estadísticas
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        try {
            // Total de libros
            long totalLibros = libroDAO.count();
            lblTotalLibros.setText(String.valueOf(totalLibros));

            // Préstamos activos
            int prestamosActivos = prestamoDAO.findByEstado(EstadoPrestamo.ACTIVO).size();
            lblPrestamosActivos.setText(String.valueOf(prestamosActivos));

            // Total usuarios
            long totalUsuarios = usuarioDAO.count();
            lblTotalUsuarios.setText(String.valueOf(totalUsuarios));

            // Multas pendientes
            long multasPendientes = multaDAO.findAll().stream()
                    .filter(m -> m.getEstadoPago().getDescripcion().equals("Pendiente"))
                    .count();
            lblMultasPendientes.setText(String.valueOf(multasPendientes));

            lblStatus.setText("✓ Estadísticas actualizadas");

        } catch (SQLException e) {
            lblStatus.setText("✗ Error al cargar estadísticas");
            AlertUtils.mostrarErrorBD(e);
        }
    }

    @FXML
    private void showDashboard() {
        lblStatus.setText("Dashboard");
        // Recargar estadísticas
        cargarEstadisticas();
    }

    @FXML
    private void showLibros() {
        loadModule("libro", "Gestión de Libros");
    }

    @FXML
    private void showEjemplares() {
        loadModule("ejemplar", "Gestión de Ejemplares");
    }

    @FXML
    private void showPrestamos() {
        loadModule("prestamo", "Gestión de Préstamos");
    }

    @FXML
    private void showUsuarios() {
        loadModule("usuario", "Gestión de Usuarios");
    }

    @FXML
    private void showMultas() {
        loadModule("multa", "Gestión de Multas");
    }

    private void loadModule(String moduleName, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/" + moduleName + ".fxml"));
            Parent moduleView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(moduleView);

            lblStatus.setText(titulo);

        } catch (IOException e) {
            AlertUtils.mostrarError("Error",
                    "No se pudo cargar el módulo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCerrarSesion() {
        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Cerrar Sesión",
                "¿Estás seguro de que deseas cerrar sesión?");

        if (confirmar) {
            try {
                LoginController.setUsuarioActual(null);
                App.loadScene("login", "Sistema de Biblioteca - Login", 600, 400);
            } catch (IOException e) {
                AlertUtils.mostrarError("Error",
                        "No se pudo volver al login: " + e.getMessage());
            }
        }
    }
}
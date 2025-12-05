package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML private Label lblUsuario;
    @FXML private Label lblStatus;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // Mostrar nombre del usuario actual
        if (LoginController.getUsuarioActual() != null) {
            lblUsuario.setText("Usuario: " + LoginController.getUsuarioActual().getNombreCompleto());
        }

        // Cargar Dashboard por defecto al iniciar
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        // Ahora sí carga la vista dashboard.fxml en el área central
        loadModule("dashboard", "Dashboard de Estadísticas");
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

    @FXML
    private void showEditoriales() {
        loadModule("editorial", "Gestión de Editoriales");
    }

    @FXML
    private void showCategorias() {
        loadModule("categoria", "Gestión de Categorías");
    }

    @FXML
    private void showUbicaciones() {
        loadModule("ubicacion", "Gestión de Ubicaciones");
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
            e.printStackTrace(); // Útil para depurar
            AlertUtils.mostrarError("Error",
                    "No se pudo cargar el módulo '" + moduleName + "': " + e.getMessage());
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
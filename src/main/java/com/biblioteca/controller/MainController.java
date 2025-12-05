package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controlador principal del sistema
 * Maneja la navegación entre módulos
 */
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private Label lblUsuario;
    @FXML private Label lblStatus;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        logger.info("Inicializando MainController");

        // Mostrar nombre del usuario actual
        if (LoginController.getUsuarioActual() != null) {
            String nombreCompleto = LoginController.getUsuarioActual().getNombreCompleto();
            lblUsuario.setText("Usuario: " + nombreCompleto);
            logger.debug("Usuario activo: {}", nombreCompleto);
        }

        // Cargar Dashboard por defecto al iniciar
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadModule("dashboard", "Dashboard de Estadísticas");
    }

    @FXML
    private void showLibros() {
        loadModule("libro", "Gestión de Libros");
    }

    @FXML
    private void showAutores() {
        loadModule("autor", "Gestión de Autores");
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
    private void showReservas() {
        loadModule("reserva", "Gestión de Reservas");
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

    /**
     * Carga un módulo FXML en el área central
     */
    private void loadModule(String moduleName, String titulo) {
        try {
            logger.debug("Cargando módulo: {}", moduleName);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/" + moduleName + ".fxml"));

            Parent moduleView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(moduleView);

            lblStatus.setText(titulo);
            logger.info("Módulo '{}' cargado exitosamente", moduleName);

        } catch (IOException e) {
            logger.error("Error al cargar módulo: {}", moduleName, e);
            AlertUtils.mostrarError("Error",
                    "No se pudo cargar el módulo '" + moduleName + "'.\n" +
                            "Verifica que el archivo FXML existe en: /view/" + moduleName + ".fxml\n\n" +
                            "Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al cargar módulo: {}", moduleName, e);
            AlertUtils.mostrarError("Error Inesperado",
                    "Ocurrió un error al cargar el módulo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCerrarSesion() {
        logger.info("Intentando cerrar sesión");

        boolean confirmar = AlertUtils.mostrarConfirmacion(
                "Cerrar Sesión",
                "¿Estás seguro de que deseas cerrar sesión?");

        if (confirmar) {
            try {
                String usuarioActual = LoginController.getUsuarioActual() != null ?
                        LoginController.getUsuarioActual().getNombreCompleto() : "Desconocido";

                logger.info("Cerrando sesión de usuario: {}", usuarioActual);

                LoginController.setUsuarioActual(null);
                App.loadScene("login", "Sistema de Biblioteca - Login", 600, 400);

                logger.info("Sesión cerrada exitosamente");

            } catch (IOException e) {
                logger.error("Error al volver al login", e);
                AlertUtils.mostrarError("Error",
                        "No se pudo volver a la pantalla de login: " + e.getMessage());
            }
        }
    }
}
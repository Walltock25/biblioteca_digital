package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Controlador para la pantalla de login
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    private static Usuario usuarioActual;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            AlertUtils.mostrarAdvertencia("Campos vacíos",
                    "Por favor ingresa tu email y contraseña");
            return;
        }

        try {
            // Intentar autenticar
            Optional<Usuario> usuarioOpt = usuarioDAO.authenticate(email, password);

            if (usuarioOpt.isPresent()) {
                usuarioActual = usuarioOpt.get();

                AlertUtils.mostrarInfo("Login Exitoso",
                        "Bienvenido " + usuarioActual.getNombreCompleto());

                // Redirigir al dashboard principal
                App.loadScene("main", "Sistema de Biblioteca - Dashboard", 1200, 700);

            } else {
                AlertUtils.mostrarError("Credenciales Inválidas",
                        "Email o contraseña incorrectos");
                passwordField.clear();
            }

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        } catch (IOException e) {
            AlertUtils.mostrarError("Error de Aplicación",
                    "No se pudo cargar la ventana principal: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        System.exit(0);
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }
}
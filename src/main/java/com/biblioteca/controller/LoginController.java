package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

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
    private void handleRegistro() {
        // 1. Crear el diálogo
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Registro de Nuevo Usuario");
        dialog.setHeaderText("Crea tu cuenta para acceder a la biblioteca");

        // 2. Configurar botones
        ButtonType registrarButton = new ButtonType("Registrarse", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registrarButton, ButtonType.CANCEL);

        // 3. Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre");
        TextField apellido = new TextField();
        apellido.setPromptText("Apellido");
        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField pass = new PasswordField();
        pass.setPromptText("Contraseña");
        TextField telefono = new TextField();
        telefono.setPromptText("Teléfono");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(apellido, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3);
        grid.add(pass, 1, 3);
        grid.add(new Label("Teléfono:"), 0, 4);
        grid.add(telefono, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 4. Convertir resultado al hacer clic en Registrarse
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registrarButton) {
                // Validar campos mínimos
                if (nombre.getText().isEmpty() || email.getText().isEmpty() || pass.getText().isEmpty()) {
                    return null; // Podrías manejar validación más estricta aquí
                }

                Usuario u = new Usuario();
                u.setNombre(nombre.getText());
                u.setApellido(apellido.getText());
                u.setEmail(email.getText());
                u.setPassword(pass.getText());
                u.setTelefono(telefono.getText());

                // ASIGNAR ROL POR DEFECTO (Asumimos ID 2 = Estudiante/Usuario)
                Rol rolDefault = new Rol();
                rolDefault.setIdRol(2); // Asegúrate de que exista el rol con ID 2 en tu BD
                u.setRol(rolDefault);

                return u;
            }
            return null;
        });

        // 5. Procesar el resultado
        Optional<Usuario> result = dialog.showAndWait();
        result.ifPresent(nuevoUsuario -> {
            try {
                if (usuarioDAO.existsByEmail(nuevoUsuario.getEmail())) {
                    AlertUtils.mostrarError("Error", "El email ya está registrado");
                } else {
                    usuarioDAO.save(nuevoUsuario);
                    AlertUtils.mostrarInfo("Éxito", "Cuenta creada correctamente. ¡Ahora puedes iniciar sesión!");
                }
            } catch (SQLException e) {
                AlertUtils.mostrarErrorBD(e);
            }
        });
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
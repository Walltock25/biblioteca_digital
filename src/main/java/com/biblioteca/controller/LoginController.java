package com.biblioteca.controller;

import com.biblioteca.App;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.AlertUtils;
import com.biblioteca.util.PasswordUtils;
import com.biblioteca.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Controlador de Login con seguridad mejorada usando BCrypt
 */
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

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

        if (!ValidationUtils.esEmailValido(email)) {
            AlertUtils.mostrarAdvertencia("Email inválido",
                    ValidationUtils.getMensajeErrorEmail());
            return;
        }

        try {
            logger.info("Intento de login para: {}", email);

            // Buscar usuario por email
            Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                logger.warn("Intento de login fallido - usuario no encontrado: {}", email);
                AlertUtils.mostrarError("Credenciales Inválidas",
                        "Email o contraseña incorrectos");
                passwordField.clear();
                return;
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar contraseña
            boolean passwordCorrecta;

            // Detectar si es hash BCrypt o texto plano (legacy)
            if (PasswordUtils.isValidBCryptHash(usuario.getPassword())) {
                // Contraseña hasheada con BCrypt
                passwordCorrecta = PasswordUtils.checkPassword(password, usuario.getPassword());
            } else {
                // Contraseña en texto plano (legacy) - comparación directa
                passwordCorrecta = usuario.getPassword().equals(password);

                // Si es correcta, actualizar a BCrypt
                if (passwordCorrecta) {
                    logger.info("Migrando contraseña legacy a BCrypt para usuario: {}", email);
                    usuario.setPassword(PasswordUtils.hashPassword(password));
                    usuarioDAO.update(usuario);
                }
            }

            if (passwordCorrecta) {
                usuarioActual = usuario;
                logger.info("Login exitoso para: {} (ID: {})", email, usuario.getIdUsuario());

                AlertUtils.mostrarInfo("Login Exitoso",
                        "Bienvenido " + usuario.getNombreCompleto());

                // Redirigir al dashboard principal
                App.loadScene("main", "Sistema de Biblioteca - Dashboard", 1200, 700);

            } else {
                logger.warn("Intento de login fallido - contraseña incorrecta: {}", email);
                AlertUtils.mostrarError("Credenciales Inválidas",
                        "Email o contraseña incorrectos");
                passwordField.clear();
            }

        } catch (SQLException e) {
            logger.error("Error de base de datos durante login", e);
            AlertUtils.mostrarErrorBD(e);
        } catch (IOException e) {
            logger.error("Error al cargar ventana principal", e);
            AlertUtils.mostrarError("Error de Aplicación",
                    "No se pudo cargar la ventana principal: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegistro() {
        logger.debug("Abriendo diálogo de registro");

        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Registro de Nuevo Usuario");
        dialog.setHeaderText("Crea tu cuenta para acceder a la biblioteca");

        ButtonType registrarButton = new ButtonType("Registrarse", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registrarButton, ButtonType.CANCEL);

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
        pass.setPromptText("Contraseña (mín. 8 caracteres)");
        PasswordField passConfirm = new PasswordField();
        passConfirm.setPromptText("Confirmar contraseña");
        TextField telefono = new TextField();
        telefono.setPromptText("Teléfono");

        Label lblFortaleza = new Label("");
        lblFortaleza.setStyle("-fx-font-size: 11px;");

        // Indicador de fortaleza de contraseña en tiempo real
        pass.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                lblFortaleza.setText("");
                return;
            }

            int nivel = PasswordUtils.evaluarFortaleza(newVal);
            String desc = PasswordUtils.getDescripcionFortaleza(nivel);
            String color = switch(nivel) {
                case 0, 1 -> "red";
                case 2 -> "orange";
                case 3, 4 -> "green";
                default -> "gray";
            };

            lblFortaleza.setText("Fortaleza: " + desc);
            lblFortaleza.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        });

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombre, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(apellido, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Contraseña:"), 0, 3);
        grid.add(pass, 1, 3);
        grid.add(lblFortaleza, 1, 4);
        grid.add(new Label("Confirmar:"), 0, 5);
        grid.add(passConfirm, 1, 5);
        grid.add(new Label("Teléfono:"), 0, 6);
        grid.add(telefono, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Validar antes de cerrar
        Button btnRegistrar = (Button) dialog.getDialogPane().lookupButton(registrarButton);
        btnRegistrar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validarRegistro(nombre.getText(), apellido.getText(), email.getText(),
                    pass.getText(), passConfirm.getText(), telefono.getText())) {
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registrarButton) {
                Usuario u = new Usuario();
                u.setNombre(nombre.getText().trim());
                u.setApellido(apellido.getText().trim());
                u.setEmail(email.getText().trim());

                // Hashear la contraseña con BCrypt
                u.setPassword(PasswordUtils.hashPassword(pass.getText()));

                u.setTelefono(telefono.getText().trim());

                // Asignar rol por defecto (Usuario = ID 3)
                Rol rolDefault = new Rol();
                rolDefault.setIdRol(3);
                u.setRol(rolDefault);

                return u;
            }
            return null;
        });

        Optional<Usuario> result = dialog.showAndWait();
        result.ifPresent(nuevoUsuario -> {
            try {
                if (usuarioDAO.existsByEmail(nuevoUsuario.getEmail())) {
                    logger.warn("Intento de registro con email duplicado: {}", nuevoUsuario.getEmail());
                    AlertUtils.mostrarError("Error", "El email ya está registrado");
                } else {
                    usuarioDAO.save(nuevoUsuario);
                    logger.info("Nuevo usuario registrado: {}", nuevoUsuario.getEmail());
                    AlertUtils.mostrarInfo("Éxito",
                            "Cuenta creada correctamente.\n¡Ahora puedes iniciar sesión!");
                }
            } catch (SQLException e) {
                logger.error("Error al registrar usuario", e);
                AlertUtils.mostrarErrorBD(e);
            }
        });
    }

    /**
     * Valida los datos del formulario de registro
     */
    private boolean validarRegistro(String nombre, String apellido, String email,
                                    String password, String passwordConfirm, String telefono) {

        if (!ValidationUtils.esTextoValido(nombre, 2, 100)) {
            AlertUtils.mostrarAdvertencia("Campo inválido",
                    "El nombre debe tener entre 2 y 100 caracteres");
            return false;
        }

        if (!ValidationUtils.esTextoValido(apellido, 2, 100)) {
            AlertUtils.mostrarAdvertencia("Campo inválido",
                    "El apellido debe tener entre 2 y 100 caracteres");
            return false;
        }

        if (!ValidationUtils.esEmailValido(email)) {
            AlertUtils.mostrarAdvertencia("Email inválido",
                    ValidationUtils.getMensajeErrorEmail());
            return false;
        }

        if (!ValidationUtils.esPasswordSegura(password)) {
            AlertUtils.mostrarAdvertencia("Contraseña débil",
                    ValidationUtils.getMensajeErrorPassword());
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            AlertUtils.mostrarAdvertencia("Contraseñas no coinciden",
                    "Las contraseñas ingresadas no coinciden");
            return false;
        }

        if (!telefono.isEmpty() && !ValidationUtils.esTelefonoValido(telefono)) {
            AlertUtils.mostrarAdvertencia("Teléfono inválido",
                    "El formato del teléfono no es válido");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        logger.info("Aplicación cerrada desde login");
        System.exit(0);
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }
}
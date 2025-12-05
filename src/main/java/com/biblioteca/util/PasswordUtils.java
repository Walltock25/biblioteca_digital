package com.biblioteca.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtils {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);

    // Factor de trabajo de BCrypt (10-12 es recomendado para producción)
    // Valores más altos = más seguro pero más lento
    private static final int WORK_FACTOR = 12;

    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        logger.debug("Generando hash BCrypt para contraseña");

        try {
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
            logger.debug("Hash generado exitosamente");
            return hashedPassword;

        } catch (Exception e) {
            logger.error("Error al generar hash de contraseña", e);
            throw new RuntimeException("Error al procesar la contraseña", e);
        }
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            logger.warn("Intento de verificación con contraseña o hash nulo");
            return false;
        }

        try {
            boolean matches = BCrypt.checkpw(plainTextPassword, hashedPassword);

            if (matches) {
                logger.debug("Contraseña verificada exitosamente");
            } else {
                logger.debug("Contraseña incorrecta");
            }

            return matches;

        } catch (Exception e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }

    public static boolean isValidBCryptHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }

        // Los hashes BCrypt empiezan con $2a$, $2b$, o $2y$
        return hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    public static String generateTemporaryPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();

        java.security.SecureRandom random = new java.security.SecureRandom();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        logger.debug("Contraseña temporal generada");
        return password.toString();
    }


    public static int evaluarFortaleza(String password) {
        if (password == null || password.length() < 6) {
            return 0; // Muy débil
        }

        int puntos = 0;

        // Longitud
        if (password.length() >= 8) puntos++;
        if (password.length() >= 12) puntos++;

        // Complejidad
        if (password.matches(".*[A-Z].*")) puntos++; // Mayúsculas
        if (password.matches(".*[a-z].*")) puntos++; // Minúsculas
        if (password.matches(".*\\d.*")) puntos++;    // Números
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) puntos++; // Especiales

        // Normalizar a escala 0-4
        return Math.min(4, puntos / 2);
    }

    public static String getDescripcionFortaleza(int nivel) {
        return switch (nivel) {
            case 0 -> "Muy Débil";
            case 1 -> "Débil";
            case 2 -> "Media";
            case 3 -> "Fuerte";
            case 4 -> "Muy Fuerte";
            default -> "Desconocida";
        };
    }
}
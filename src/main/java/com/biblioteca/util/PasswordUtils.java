package com.biblioteca.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidades para manejo seguro de contraseñas usando BCrypt
 *
 * BCrypt es un algoritmo de hashing adaptativo que:
 * - Genera un salt aleatorio para cada contraseña
 * - Es resistente a ataques de fuerza bruta
 * - Puede ajustar el factor de trabajo según la potencia computacional
 */
public class PasswordUtils {

    private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);

    // Factor de trabajo de BCrypt (10-12 es recomendado para producción)
    // Valores más altos = más seguro pero más lento
    private static final int WORK_FACTOR = 12;

    /**
     * Genera un hash seguro de una contraseña usando BCrypt
     *
     * @param plainTextPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
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

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt
     *
     * @param plainTextPassword Contraseña en texto plano a verificar
     * @param hashedPassword Hash BCrypt almacenado
     * @return true si la contraseña coincide, false en caso contrario
     */
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

    /**
     * Verifica si un hash es válido BCrypt
     *
     * @param hash String a verificar
     * @return true si es un hash BCrypt válido
     */
    public static boolean isValidBCryptHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }

        // Los hashes BCrypt empiezan con $2a$, $2b$, o $2y$
        return hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    /**
     * Genera una contraseña temporal aleatoria
     * Útil para funciones de "olvidé mi contraseña"
     *
     * @return Contraseña temporal de 12 caracteres
     */
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

    /**
     * Verifica la fortaleza de una contraseña
     *
     * @param password Contraseña a evaluar
     * @return Nivel de fortaleza (0=muy débil, 4=muy fuerte)
     */
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

    /**
     * Obtiene descripción textual de la fortaleza
     *
     * @param nivel Nivel de fortaleza (0-4)
     * @return Descripción textual
     */
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
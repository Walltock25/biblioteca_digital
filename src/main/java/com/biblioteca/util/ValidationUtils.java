package com.biblioteca.util;

import java.util.regex.Pattern;

/**
 * Utilidades para validación de datos de entrada
 * Centraliza todas las validaciones para mantener consistencia
 */
public class ValidationUtils {

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern ISBN_10_PATTERN = Pattern.compile(
            "^(?:\\d{9}X|\\d{10})$"
    );

    private static final Pattern ISBN_13_PATTERN = Pattern.compile(
            "^97[89]\\d{10}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );

    /**
     * Valida que un email tenga formato correcto
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida formato de ISBN (10 o 13 dígitos)
     */
    public static boolean esISBNValido(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        String isbnLimpio = isbn.replaceAll("[\\s-]", "");

        return ISBN_10_PATTERN.matcher(isbnLimpio).matches() ||
                ISBN_13_PATTERN.matcher(isbnLimpio).matches();
    }

    /**
     * Valida formato de teléfono
     */
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return true; // Campo opcional
        }

        String telefonoLimpio = telefono.replaceAll("[\\s()-]", "");
        return TELEFONO_PATTERN.matcher(telefonoLimpio).matches();
    }

    /**
     * Valida que un texto esté dentro de un rango de longitud
     */
    public static boolean esTextoValido(String texto, int longitudMinima, int longitudMaxima) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        int longitud = texto.trim().length();
        return longitud >= longitudMinima && longitud <= longitudMaxima;
    }

    /**
     * Valida que un año esté en un rango razonable
     */
    public static boolean esAnioValido(int anio) {
        int anioActual = java.time.Year.now().getValue();
        return anio >= 1000 && anio <= anioActual + 1;
    }

    /**
     * Valida que una contraseña cumpla requisitos mínimos
     */
    public static boolean esPasswordSegura(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*\\d.*");

        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    /**
     * Valida que un campo numérico sea positivo
     */
    public static boolean esNumeroPositivo(Number numero) {
        if (numero == null) {
            return false;
        }
        return numero.doubleValue() > 0;
    }

    /**
     * Valida que un String pueda convertirse a número
     */
    public static boolean esNumeroValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(texto.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida que un String pueda convertirse a entero
     */
    public static boolean esEnteroValido(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(texto.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sanitiza un texto para evitar inyección SQL básica
     */
    public static String sanitizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.trim()
                .replaceAll("[<>]", "") // Eliminar caracteres potencialmente peligrosos
                .replaceAll("\\s+", " "); // Normalizar espacios
    }

    /**
     * Valida formato de código de barras (numérico de 8-13 dígitos)
     */
    public static boolean esCodigoBarrasValido(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }

        String codigoLimpio = codigo.replaceAll("[\\s-]", "");
        return codigoLimpio.matches("^\\d{8,13}$");
    }

    /**
     * Mensaje de error personalizado para validación de email
     */
    public static String getMensajeErrorEmail() {
        return "El email debe tener un formato válido (ejemplo: usuario@dominio.com)";
    }

    /**
     * Mensaje de error personalizado para validación de ISBN
     */
    public static String getMensajeErrorISBN() {
        return "El ISBN debe tener 10 o 13 dígitos (puede incluir guiones)";
    }

    /**
     * Mensaje de error personalizado para validación de password
     */
    public static String getMensajeErrorPassword() {
        return "La contraseña debe tener al menos 8 caracteres, " +
                "incluyendo mayúsculas, minúsculas y números";
    }
}
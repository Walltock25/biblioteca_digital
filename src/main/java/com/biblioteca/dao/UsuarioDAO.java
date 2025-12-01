package com.biblioteca.dao;

import com.biblioteca.model.Usuario;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Interfaz para operaciones con Usuarios
 */
public interface UsuarioDAO extends GenericDAO<Usuario, Integer> {

    /**
     * Busca un usuario por su email (clave natural única)
     */
    Optional<Usuario> findByEmail(String email) throws SQLException;

    /**
     * Autentica un usuario por email y contraseña
     */
    Optional<Usuario> authenticate(String email, String password) throws SQLException;

    /**
     * Verifica si un email ya está registrado
     */
    boolean existsByEmail(String email) throws SQLException;
}
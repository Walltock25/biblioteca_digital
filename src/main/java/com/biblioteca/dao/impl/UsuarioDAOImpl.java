package com.biblioteca.dao.impl;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String INSERT =
            "INSERT INTO Usuarios (nombre, apellido, email, password, telefono, fecha_registro, id_rol) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE Usuarios SET nombre = ?, apellido = ?, email = ?, password = ?, " +
                    "telefono = ?, id_rol = ? WHERE id_usuario = ?";

    private static final String DELETE = "DELETE FROM Usuarios WHERE id_usuario = ?";

    private static final String SELECT_BY_ID =
            "SELECT u.*, r.nombre_rol FROM Usuarios u " +
                    "INNER JOIN Roles r ON u.id_rol = r.id_rol WHERE u.id_usuario = ?";

    private static final String SELECT_BY_EMAIL =
            "SELECT u.*, r.nombre_rol FROM Usuarios u " +
                    "INNER JOIN Roles r ON u.id_rol = r.id_rol WHERE u.email = ?";

    private static final String SELECT_ALL =
            "SELECT u.*, r.nombre_rol FROM Usuarios u " +
                    "INNER JOIN Roles r ON u.id_rol = r.id_rol ORDER BY u.apellido, u.nombre";

    @Override
    public Integer save(Usuario usuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getPassword());
            stmt.setString(5, usuario.getTelefono());
            stmt.setDate(6, Date.valueOf(usuario.getFechaRegistro()));
            stmt.setInt(7, usuario.getRol().getIdRol());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo guardar el usuario");
    }

    @Override
    public boolean update(Usuario usuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getPassword());
            stmt.setString(5, usuario.getTelefono());
            stmt.setInt(6, usuario.getRol().getIdRol());
            stmt.setInt(7, usuario.getIdUsuario());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Usuario> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> authenticate(String email, String password) throws SQLException {
        Optional<Usuario> usuarioOpt = findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // En producción, usar BCrypt para comparar contraseñas hasheadas
            if (usuario.getPassword().equals(password)) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }
        return usuarios;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Usuarios";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        return findById(id).isPresent();
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());

        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        usuario.setRol(rol);

        return usuario;
    }
}
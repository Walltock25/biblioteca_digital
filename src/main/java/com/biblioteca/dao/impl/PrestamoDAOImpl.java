package com.biblioteca.dao.impl;

import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.model.*;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrestamoDAOImpl implements PrestamoDAO {

    private static final String INSERT =
            "INSERT INTO Prestamos (id_usuario, id_ejemplar, fecha_salida, " +
                    "fecha_devolucion_esperada, fecha_devolucion_real, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE Prestamos SET fecha_devolucion_real = ?, estado = ? WHERE id_prestamo = ?";

    private static final String SELECT_BY_ID =
            "SELECT p.*, u.nombre, u.apellido, e.codigo_barras, l.titulo " +
                    "FROM Prestamos p " +
                    "INNER JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                    "INNER JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE p.id_prestamo = ?";

    private static final String SELECT_BY_USUARIO_AND_ESTADO =
            "SELECT p.*, u.nombre, u.apellido, e.codigo_barras, l.titulo " +
                    "FROM Prestamos p " +
                    "INNER JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                    "INNER JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE p.id_usuario = ? AND p.estado = ?";

    private static final String COUNT_BY_USUARIO_AND_ESTADO =
            "SELECT COUNT(*) FROM Prestamos WHERE id_usuario = ? AND estado = ?";

    @Override
    public Integer save(Prestamo prestamo) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, prestamo.getUsuario().getIdUsuario());
            stmt.setInt(2, prestamo.getEjemplar().getIdEjemplar());
            stmt.setTimestamp(3, Timestamp.valueOf(prestamo.getFechaSalida()));
            stmt.setTimestamp(4, Timestamp.valueOf(prestamo.getFechaDevolucionEsperada()));
            stmt.setTimestamp(5, prestamo.getFechaDevolucionReal() != null ?
                    Timestamp.valueOf(prestamo.getFechaDevolucionReal()) : null);
            stmt.setString(6, prestamo.getEstado().getDescripcion());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo guardar el préstamo");
    }

    @Override
    public boolean update(Prestamo prestamo) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setTimestamp(1, prestamo.getFechaDevolucionReal() != null ?
                    Timestamp.valueOf(prestamo.getFechaDevolucionReal()) : null);
            stmt.setString(2, prestamo.getEstado().getDescripcion());
            stmt.setInt(3, prestamo.getIdPrestamo());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Prestamos WHERE id_prestamo = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Prestamo> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrestamo(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Prestamo> findByUsuarioAndEstado(Integer idUsuario, EstadoPrestamo estado)
            throws SQLException {
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_AND_ESTADO)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, estado.getDescripcion());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
        }
        return prestamos;
    }

    @Override
    public List<Prestamo> findByUsuario(Integer idUsuario) throws SQLException {
        String sql = "SELECT p.*, u.nombre, u.apellido, e.codigo_barras, l.titulo " +
                "FROM Prestamos p " +
                "INNER JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                "INNER JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                "WHERE p.id_usuario = ? ORDER BY p.fecha_salida DESC";

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
        }
        return prestamos;
    }

    @Override
    public List<Prestamo> findByEstado(EstadoPrestamo estado) throws SQLException {
        String sql = "SELECT p.*, u.nombre, u.apellido, e.codigo_barras, l.titulo " +
                "FROM Prestamos p " +
                "INNER JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                "INNER JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                "WHERE p.estado = ?";

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.getDescripcion());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
        }
        return prestamos;
    }

    @Override
    public int countPrestamosByUsuarioAndEstado(Integer idUsuario, EstadoPrestamo estado)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_USUARIO_AND_ESTADO)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, estado.getDescripcion());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    @Override
    public List<Prestamo> findAll() throws SQLException {
        String sql = "SELECT p.*, u.nombre, u.apellido, e.codigo_barras, l.titulo " +
                "FROM Prestamos p " +
                "INNER JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                "INNER JOIN Ejemplares e ON p.id_ejemplar = e.id_ejemplar " +
                "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                "ORDER BY p.fecha_salida DESC";

        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prestamos.add(mapResultSetToPrestamo(rs));
            }
        }
        return prestamos;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Prestamos";
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

    private Prestamo mapResultSetToPrestamo(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("id_prestamo"));
        prestamo.setFechaSalida(rs.getTimestamp("fecha_salida").toLocalDateTime());
        prestamo.setFechaDevolucionEsperada(rs.getTimestamp("fecha_devolucion_esperada").toLocalDateTime());

        Timestamp devolucionReal = rs.getTimestamp("fecha_devolucion_real");
        if (devolucionReal != null) {
            prestamo.setFechaDevolucionReal(devolucionReal.toLocalDateTime());
        }

        prestamo.setEstado(EstadoPrestamo.fromString(rs.getString("estado")));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        prestamo.setUsuario(usuario);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(rs.getInt("id_ejemplar"));
        ejemplar.setCodigoBarras(rs.getString("codigo_barras"));

        Libro libro = new Libro();
        libro.setTitulo(rs.getString("titulo"));
        ejemplar.setLibro(libro);
        prestamo.setEjemplar(ejemplar);

        return prestamo;
    }
}
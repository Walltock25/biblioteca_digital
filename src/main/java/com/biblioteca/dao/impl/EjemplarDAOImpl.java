package com.biblioteca.dao.impl;

import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Ubicacion;
import com.biblioteca.model.enums.EstadoFisico;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EjemplarDAOImpl implements EjemplarDAO {

    private static final String INSERT =
            "INSERT INTO Ejemplares (codigo_barras, id_libro, id_ubicacion, estado_fisico, disponible) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE Ejemplares SET codigo_barras = ?, id_libro = ?, id_ubicacion = ?, " +
                    "estado_fisico = ?, disponible = ? WHERE id_ejemplar = ?";

    private static final String SELECT_BY_ID =
            "SELECT e.*, l.titulo, l.isbn " +
                    "FROM Ejemplares e " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE e.id_ejemplar = ?";

    private static final String SELECT_BY_CODIGO =
            "SELECT e.*, l.titulo, l.isbn " +
                    "FROM Ejemplares e " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE e.codigo_barras = ?";

    private static final String SELECT_BY_LIBRO =
            "SELECT e.*, l.titulo, l.isbn " +
                    "FROM Ejemplares e " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE e.id_libro = ?";

    private static final String SELECT_DISPONIBLES_BY_LIBRO =
            "SELECT e.*, l.titulo, l.isbn " +
                    "FROM Ejemplares e " +
                    "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                    "WHERE e.id_libro = ? AND e.disponible = TRUE";

    @Override
    public Integer save(Ejemplar ejemplar) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ejemplar.getCodigoBarras());
            stmt.setInt(2, ejemplar.getLibro().getIdLibro());
            stmt.setInt(3, ejemplar.getUbicacion() != null ? ejemplar.getUbicacion().getIdUbicacion() : null);
            stmt.setString(4, ejemplar.getEstadoFisico().getDescripcion());
            stmt.setBoolean(5, ejemplar.getDisponible());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo guardar el ejemplar");
    }

    @Override
    public boolean update(Ejemplar ejemplar) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setString(1, ejemplar.getCodigoBarras());
            stmt.setInt(2, ejemplar.getLibro().getIdLibro());

            if (ejemplar.getUbicacion() != null) {
                stmt.setInt(3, ejemplar.getUbicacion().getIdUbicacion());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, ejemplar.getEstadoFisico().getDescripcion());
            stmt.setBoolean(5, ejemplar.getDisponible());
            stmt.setInt(6, ejemplar.getIdEjemplar());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Ejemplares WHERE id_ejemplar = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Ejemplar> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEjemplar(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Ejemplar> findByCodigoBarras(String codigoBarras) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODIGO)) {

            stmt.setString(1, codigoBarras);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEjemplar(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Ejemplar> findByLibro(Integer idLibro) throws SQLException {
        List<Ejemplar> ejemplares = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_LIBRO)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ejemplares.add(mapResultSetToEjemplar(rs));
                }
            }
        }
        return ejemplares;
    }

    @Override
    public List<Ejemplar> findDisponiblesByLibro(Integer idLibro) throws SQLException {
        List<Ejemplar> ejemplares = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_DISPONIBLES_BY_LIBRO)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ejemplares.add(mapResultSetToEjemplar(rs));
                }
            }
        }
        return ejemplares;
    }

    @Override
    public List<Ejemplar> findAll() throws SQLException {
        String sql = "SELECT e.*, l.titulo, l.isbn " +
                "FROM Ejemplares e " +
                "INNER JOIN Libros l ON e.id_libro = l.id_libro " +
                "ORDER BY l.titulo";

        List<Ejemplar> ejemplares = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ejemplares.add(mapResultSetToEjemplar(rs));
            }
        }
        return ejemplares;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Ejemplares";
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

    private Ejemplar mapResultSetToEjemplar(ResultSet rs) throws SQLException {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setIdEjemplar(rs.getInt("id_ejemplar"));
        ejemplar.setCodigoBarras(rs.getString("codigo_barras"));
        ejemplar.setEstadoFisico(EstadoFisico.fromString(rs.getString("estado_fisico")));
        ejemplar.setDisponible(rs.getBoolean("disponible"));

        Libro libro = new Libro();
        libro.setIdLibro(rs.getInt("id_libro"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setIsbn(rs.getString("isbn"));
        ejemplar.setLibro(libro);

        // Ubicación se cargaría si es necesario

        return ejemplar;
    }
}
package com.biblioteca.dao.impl;

import com.biblioteca.dao.ReservaDAO;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO de Reservas
 */
public class ReservaDAOImpl implements ReservaDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReservaDAOImpl.class);

    private static final String INSERT =
            "INSERT INTO Reservas (id_usuario, id_libro, fecha_reserva, estado) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE Reservas SET estado = ? WHERE id_reserva = ?";

    private static final String DELETE =
            "DELETE FROM Reservas WHERE id_reserva = ?";

    private static final String SELECT_BY_ID =
            "SELECT r.*, u.nombre, u.apellido, l.titulo, l.isbn " +
                    "FROM Reservas r " +
                    "INNER JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                    "INNER JOIN Libros l ON r.id_libro = l.id_libro " +
                    "WHERE r.id_reserva = ?";

    private static final String SELECT_ALL =
            "SELECT r.*, u.nombre, u.apellido, l.titulo, l.isbn " +
                    "FROM Reservas r " +
                    "INNER JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                    "INNER JOIN Libros l ON r.id_libro = l.id_libro " +
                    "ORDER BY r.fecha_reserva DESC";

    private static final String SELECT_BY_USUARIO_AND_ESTADO =
            "SELECT r.*, u.nombre, u.apellido, l.titulo, l.isbn " +
                    "FROM Reservas r " +
                    "INNER JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                    "INNER JOIN Libros l ON r.id_libro = l.id_libro " +
                    "WHERE r.id_usuario = ? AND r.estado = ?";

    private static final String SELECT_BY_USUARIO =
            "SELECT r.*, u.nombre, u.apellido, l.titulo, l.isbn " +
                    "FROM Reservas r " +
                    "INNER JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                    "INNER JOIN Libros l ON r.id_libro = l.id_libro " +
                    "WHERE r.id_usuario = ? ORDER BY r.fecha_reserva DESC";

    private static final String SELECT_BY_LIBRO =
            "SELECT r.*, u.nombre, u.apellido, l.titulo, l.isbn " +
                    "FROM Reservas r " +
                    "INNER JOIN Usuarios u ON r.id_usuario = u.id_usuario " +
                    "INNER JOIN Libros l ON r.id_libro = l.id_libro " +
                    "WHERE r.id_libro = ? ORDER BY r.fecha_reserva ASC";

    private static final String CHECK_RESERVA_ACTIVA =
            "SELECT COUNT(*) FROM Reservas " +
                    "WHERE id_usuario = ? AND id_libro = ? AND estado = 'Pendiente'";

    private static final String COUNT_RESERVAS_ACTIVAS =
            "SELECT COUNT(*) FROM Reservas WHERE id_usuario = ? AND estado = 'Pendiente'";

    @Override
    public Integer save(Reserva reserva) throws SQLException {
        logger.debug("Guardando reserva de usuario {} para libro {}",
                reserva.getUsuario().getIdUsuario(), reserva.getLibro().getIdLibro());

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reserva.getUsuario().getIdUsuario());
            stmt.setInt(2, reserva.getLibro().getIdLibro());
            stmt.setTimestamp(3, Timestamp.valueOf(reserva.getFechaReserva()));
            stmt.setString(4, reserva.getEstado().getDescripcion());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer id = generatedKeys.getInt(1);
                    logger.info("Reserva guardada con ID: {}", id);
                    return id;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al guardar reserva", e);
            throw e;
        }
    }

    @Override
    public boolean update(Reserva reserva) throws SQLException {
        logger.debug("Actualizando reserva ID: {}", reserva.getIdReserva());

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setString(1, reserva.getEstado().getDescripcion());
            stmt.setInt(2, reserva.getIdReserva());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                logger.info("Reserva actualizada: {}", reserva.getIdReserva());
            }

            return updated;

        } catch (SQLException e) {
            logger.error("Error al actualizar reserva", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        logger.debug("Eliminando reserva ID: {}", id);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {

            stmt.setInt(1, id);
            boolean deleted = stmt.executeUpdate() > 0;

            if (deleted) {
                logger.info("Reserva eliminada: {}", id);
            }

            return deleted;

        } catch (SQLException e) {
            logger.error("Error al eliminar reserva", e);
            throw e;
        }
    }

    @Override
    public Optional<Reserva> findById(Integer id) throws SQLException {
        logger.debug("Buscando reserva ID: {}", id);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReserva(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar reserva por ID", e);
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<Reserva> findAll() throws SQLException {
        logger.debug("Obteniendo todas las reservas");

        List<Reserva> reservas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservas.add(mapResultSetToReserva(rs));
            }

            logger.debug("Se encontraron {} reservas", reservas.size());

        } catch (SQLException e) {
            logger.error("Error al obtener todas las reservas", e);
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> findByUsuarioAndEstado(Integer idUsuario, EstadoReserva estado)
            throws SQLException {
        logger.debug("Buscando reservas de usuario {} con estado {}", idUsuario, estado);

        List<Reserva> reservas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_AND_ESTADO)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, estado.getDescripcion());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReserva(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar reservas por usuario y estado", e);
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> findByUsuario(Integer idUsuario) throws SQLException {
        logger.debug("Buscando reservas del usuario {}", idUsuario);

        List<Reserva> reservas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReserva(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar reservas por usuario", e);
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> findByLibro(Integer idLibro) throws SQLException {
        logger.debug("Buscando reservas del libro {}", idLibro);

        List<Reserva> reservas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_LIBRO)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReserva(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar reservas por libro", e);
            throw e;
        }

        return reservas;
    }

    @Override
    public List<Reserva> findReservasPendientesByLibro(Integer idLibro) throws SQLException {
        return findByUsuarioAndEstado(idLibro, EstadoReserva.PENDIENTE);
    }

    @Override
    public boolean usuarioTieneReservaActiva(Integer idUsuario, Integer idLibro)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(CHECK_RESERVA_ACTIVA)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    @Override
    public int countReservasActivasByUsuario(Integer idUsuario) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_RESERVAS_ACTIVAS)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reservas";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error al contar reservas", e);
            throw e;
        }

        return 0;
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        return findById(id).isPresent();
    }

    private Reserva mapResultSetToReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getInt("id_reserva"));
        reserva.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
        reserva.setEstado(EstadoReserva.fromString(rs.getString("estado")));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        reserva.setUsuario(usuario);

        Libro libro = new Libro();
        libro.setIdLibro(rs.getInt("id_libro"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setIsbn(rs.getString("isbn"));
        reserva.setLibro(libro);

        return reserva;
    }
}
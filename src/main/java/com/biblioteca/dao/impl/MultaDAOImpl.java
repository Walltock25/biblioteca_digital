package com.biblioteca.dao.impl;

import com.biblioteca.dao.MultaDAO;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoPago;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MultaDAOImpl implements MultaDAO {

    private static final String INSERT =
            "INSERT INTO Multas (id_prestamo, monto, motivo, fecha_generacion, estado_pago) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE Multas SET monto = ?, motivo = ?, estado_pago = ? WHERE id_multa = ?";

    private static final String SELECT_BY_ID =
            "SELECT * FROM Multas WHERE id_multa = ?";

    private static final String SELECT_BY_USUARIO_AND_ESTADO =
            "SELECT m.* FROM Multas m " +
                    "INNER JOIN Prestamos p ON m.id_prestamo = p.id_prestamo " +
                    "WHERE p.id_usuario = ? AND m.estado_pago = ?";

    private static final String SELECT_BY_PRESTAMO =
            "SELECT * FROM Multas WHERE id_prestamo = ?";

    @Override
    public Integer save(Multa multa) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, multa.getPrestamo().getIdPrestamo());
            stmt.setBigDecimal(2, multa.getMonto());
            stmt.setString(3, multa.getMotivo());
            stmt.setTimestamp(4, Timestamp.valueOf(multa.getFechaGeneracion()));
            stmt.setString(5, multa.getEstadoPago().getDescripcion());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo guardar la multa");
    }

    @Override
    public boolean update(Multa multa) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setBigDecimal(1, multa.getMonto());
            stmt.setString(2, multa.getMotivo());
            stmt.setString(3, multa.getEstadoPago().getDescripcion());
            stmt.setInt(4, multa.getIdMulta());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM Multas WHERE id_multa = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Multa> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMulta(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Multa> findByUsuarioAndEstado(Integer idUsuario, String estadoPago) throws SQLException {
        List<Multa> multas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_AND_ESTADO)) {

            stmt.setInt(1, idUsuario);
            stmt.setString(2, estadoPago);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapResultSetToMulta(rs));
                }
            }
        }
        return multas;
    }

    @Override
    public List<Multa> findByUsuario(Integer idUsuario) throws SQLException {
        String sql = "SELECT m.* FROM Multas m " +
                "INNER JOIN Prestamos p ON m.id_prestamo = p.id_prestamo " +
                "WHERE p.id_usuario = ?";

        List<Multa> multas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    multas.add(mapResultSetToMulta(rs));
                }
            }
        }
        return multas;
    }

    @Override
    public Multa findByPrestamo(Integer idPrestamo) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_PRESTAMO)) {

            stmt.setInt(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMulta(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Multa> findAll() throws SQLException {
        String sql = "SELECT * FROM Multas ORDER BY fecha_generacion DESC";
        List<Multa> multas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                multas.add(mapResultSetToMulta(rs));
            }
        }
        return multas;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Multas";
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

    private Multa mapResultSetToMulta(ResultSet rs) throws SQLException {
        Multa multa = new Multa();
        multa.setIdMulta(rs.getInt("id_multa"));
        multa.setMonto(rs.getBigDecimal("monto"));
        multa.setMotivo(rs.getString("motivo"));
        multa.setFechaGeneracion(rs.getTimestamp("fecha_generacion").toLocalDateTime());
        multa.setEstadoPago(EstadoPago.fromString(rs.getString("estado_pago")));

        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("id_prestamo"));
        multa.setPrestamo(prestamo);

        return multa;
    }
}
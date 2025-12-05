package com.biblioteca.dao.impl;

import com.biblioteca.dao.UbicacionDAO;
import com.biblioteca.model.Ubicacion;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UbicacionDAOImpl implements UbicacionDAO {

    @Override
    public Integer save(Ubicacion u) throws SQLException {
        String sql = "INSERT INTO Ubicaciones (pasillo, estante, piso) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, u.getPasillo());
            stmt.setString(2, u.getEstante());
            stmt.setInt(3, u.getPiso());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    @Override
    public List<Ubicacion> findAll() throws SQLException {
        List<Ubicacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM Ubicaciones ORDER BY piso, pasillo, estante";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ubicacion u = new Ubicacion();
                u.setIdUbicacion(rs.getInt("id_ubicacion"));
                u.setPasillo(rs.getString("pasillo"));
                u.setEstante(rs.getString("estante"));
                u.setPiso(rs.getInt("piso"));
                lista.add(u);
            }
        }
        return lista;
    }

    // Métodos obligatorios de la interfaz GenericDAO (puedes dejarlos básicos por ahora)
    @Override public boolean update(Ubicacion u) throws SQLException { return false; }
    @Override public boolean delete(Integer id) throws SQLException { return false; }
    @Override public Optional<Ubicacion> findById(Integer id) throws SQLException { return Optional.empty(); }
    @Override public long count() throws SQLException { return 0; }
    @Override public boolean existsById(Integer id) throws SQLException { return false; }
}
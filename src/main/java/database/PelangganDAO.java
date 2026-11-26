package main.java.database;

import java.sql.*;
import java.util.*;
import main.java.model.Pelanggan;

public class PelangganDAO {

    public List<Pelanggan> getAllPelanggan() throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT id, nama, alamat FROM pelanggan ORDER BY id";
        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Pelanggan(rs.getInt("id"), rs.getString("nama"),
                        rs.getString("alamat")));
            }
        }
        return list;
    }

    public void savePelanggan(Pelanggan p) throws SQLException {
        String sql = "INSERT INTO pelanggan (nama, alamat) VALUES (?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNama());
            stmt.setString(2, p.getAlamat());
            stmt.executeUpdate();
        }
    }

    public void updatePelanggan(Pelanggan p, int id) throws SQLException {
        String sql = "UPDATE pelanggan SET nama=?, alamat=? WHERE id=?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNama());
            stmt.setString(2, p.getAlamat());
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }

    public void deletePelanggan(int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM pelanggan WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
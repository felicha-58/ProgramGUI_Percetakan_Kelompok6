package main.java.database;

import java.sql.*;
import java.util.*;
import main.java.model.*;

public class CetakanDAO {

    public List<Cetakan> getAllCetakan() throws SQLException {
        List<Cetakan> list = new ArrayList<>();
        String sql = "SELECT c.id, c.kode, c.nama, c.tipe, c.ukuran, c.harga_satuan, " +
                     "h.bentuk, h.jenis_tali, " +
                     "b.jumlah_halaman, b.jenis_kertas AS brosur_jk, " +
                     "k.jenis_kertas AS kn_jk, k.finishing " +
                     "FROM cetakan c " +
                     "LEFT JOIN \"Hangtag\" h ON c.id = h.hangtag_id " +
                     "LEFT JOIN \"Brosur\" b ON c.id = b.brosur_id " +
                     "LEFT JOIN \"KartuNama\" k ON c.id = k.kartunama_id " +
                     "ORDER BY c.id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String tipe = rs.getString("tipe");
                int id = rs.getInt("id");
                String kode = rs.getString("kode");
                String nama = rs.getString("nama");
                String ukuran = rs.getString("ukuran");
                double harga = rs.getDouble("harga_satuan");
                if ("Hangtag".equals(tipe)) {
                    list.add(new Hangtag(id, kode, nama, ukuran, harga,
                            rs.getString("bentuk"), rs.getString("jenis_tali")));
                } else if ("Brosur".equals(tipe)) {
                    list.add(new Brosur(id, kode, nama, ukuran, harga,
                            rs.getInt("jumlah_halaman"), rs.getString("brosur_jk")));
                } else if ("KartuNama".equals(tipe)) {
                    list.add(new KartuNama(id, kode, nama, ukuran, harga,
                            rs.getString("kn_jk"), rs.getString("finishing")));
                }
            }
        }
        return list;
    }

    public void saveCetakan(Cetakan c) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO cetakan (kode, nama, tipe, ukuran, harga_satuan) " +
                             "VALUES (?, ?, ?, ?, ?)";
                int cid;
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, c.getKode());
                    pstmt.setString(2, c.getNama());
                    pstmt.setString(3, c.getTipe());
                    pstmt.setString(4, c.getUkuran());
                    pstmt.setDouble(5, c.getHargaSatuan());
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        keys.next();
                        cid = keys.getInt(1);
                    }
                }
                insertSubclass(conn, c, cid);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateCetakan(Cetakan c, int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "UPDATE cetakan SET kode=?, nama=?, tipe=?, ukuran=?, harga_satuan=? WHERE id=?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, c.getKode());
                    pstmt.setString(2, c.getNama());
                    pstmt.setString(3, c.getTipe());
                    pstmt.setString(4, c.getUkuran());
                    pstmt.setDouble(5, c.getHargaSatuan());
                    pstmt.setInt(6, id);
                    pstmt.executeUpdate();
                }
                if (c instanceof Hangtag) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE \"Hangtag\" SET bentuk=?, jenis_tali=? WHERE hangtag_id=?")) {
                        pstmt.setString(1, ((Hangtag) c).getBentuk());
                        pstmt.setString(2, ((Hangtag) c).getJenisTali());
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();
                    }
                } else if (c instanceof Brosur) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE \"Brosur\" SET jumlah_halaman=?, jenis_kertas=? WHERE brosur_id=?")) {
                        pstmt.setInt(1, ((Brosur) c).getJumlahHalaman());
                        pstmt.setString(2, ((Brosur) c).getJenisKertas());
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();
                    }
                } else if (c instanceof KartuNama) {
                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "UPDATE \"KartuNama\" SET jenis_kertas=?, finishing=? WHERE kartunama_id=?")) {
                        pstmt.setString(1, ((KartuNama) c).getJenisKertas());
                        pstmt.setString(2, ((KartuNama) c).getFinishing());
                        pstmt.setInt(3, id);
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void insertSubclass(Connection conn, Cetakan c, int cid) throws SQLException {
        if (c instanceof Hangtag) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"Hangtag\" (hangtag_id, bentuk, jenis_tali) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, cid);
                pstmt.setString(2, ((Hangtag) c).getBentuk());
                pstmt.setString(3, ((Hangtag) c).getJenisTali());
                pstmt.executeUpdate();
            }
        } else if (c instanceof Brosur) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"Brosur\" (brosur_id, jumlah_halaman, jenis_kertas) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, cid);
                pstmt.setInt(2, ((Brosur) c).getJumlahHalaman());
                pstmt.setString(3, ((Brosur) c).getJenisKertas());
                pstmt.executeUpdate();
            }
        } else if (c instanceof KartuNama) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO \"KartuNama\" (kartunama_id, jenis_kertas, finishing) VALUES (?, ?, ?)")) {
                pstmt.setInt(1, cid);
                pstmt.setString(2, ((KartuNama) c).getJenisKertas());
                pstmt.setString(3, ((KartuNama) c).getFinishing());
                pstmt.executeUpdate();
            }
        }
    }

    public void deleteCetakan(int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cetakan WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
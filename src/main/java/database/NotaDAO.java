package main.java.database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;

public class NotaDAO {

    public List<Nota> getAllNota() throws SQLException {
        List<Nota> list = new ArrayList<>();
        String sql = "SELECT n.id, n.nomor_nota, n.tanggal, " +
                     "p.id AS pid, p.nama AS pnama, p.alamat " +
                     "FROM \"Nota\" n " +
                     "LEFT JOIN pelanggan p ON n.pelanggan_id = p.id " +
                     "ORDER BY n.id DESC";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Pelanggan p = new Pelanggan(
                        rs.getInt("pid"),
                        rs.getString("pnama"),
                        rs.getString("alamat"));
                Nota n = new Nota(
                        rs.getInt("id"),
                        rs.getString("nomor_nota"),
                        rs.getObject("tanggal", LocalDate.class),
                        p);
                list.add(n);
            }
        }

        for (Nota n : list) {
            n.setItems(loadItems(n.getId()));
        }
        return list;
    }

    private ObservableList<NotaItem> loadItems(int notaId) throws SQLException {
        ObservableList<NotaItem> items = FXCollections.observableArrayList();
        String sql = "SELECT ni.id, ni.jumlah, " +
                     "c.id AS cid, c.kode, c.nama, c.tipe, c.ukuran, c.harga_satuan, " +
                     "h.bentuk, h.jenis_tali, " +
                     "b.jumlah_halaman, b.jenis_kertas AS b_jk, " +
                     "k.jenis_kertas AS k_jk, k.finishing " +
                     "FROM \"NotaItem\" ni " +
                     "JOIN cetakan c ON ni.cetakan_id = c.id " +
                     "LEFT JOIN \"Hangtag\" h  ON c.id = h.hangtag_id " +
                     "LEFT JOIN \"Brosur\" b   ON c.id = b.brosur_id " +
                     "LEFT JOIN \"KartuNama\" k ON c.id = k.kartunama_id " +
                     "WHERE ni.nota_id = ? ORDER BY ni.id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Cetakan c = buildCetakan(rs);
                    items.add(new NotaItem(rs.getInt("id"), c, rs.getInt("jumlah")));
                }
            }
        }
        return items;
    }

    private Cetakan buildCetakan(ResultSet rs) throws SQLException {
        String tipe = rs.getString("tipe");
        int cid = rs.getInt("cid");
        String kode = rs.getString("kode");
        String nama = rs.getString("nama");
        String ukuran = rs.getString("ukuran");
        double harga = rs.getDouble("harga_satuan");

        if ("Hangtag".equals(tipe)) {
            return new Hangtag(cid, kode, nama, ukuran, harga,
                    rs.getString("bentuk"), rs.getString("jenis_tali"));
        } else if ("Brosur".equals(tipe)) {
            return new Brosur(cid, kode, nama, ukuran, harga,
                    rs.getInt("jumlah_halaman"), rs.getString("b_jk"));
        } else {
            return new KartuNama(cid, kode, nama, ukuran, harga,
                    rs.getString("k_jk"), rs.getString("finishing"));
        }
    }

    public void saveNota(Nota n) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int notaId;
                String sql = "INSERT INTO \"Nota\" (nomor_nota, tanggal, pelanggan_id) " +
                             "VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, n.getNomorNota());
                    pstmt.setObject(2, n.getTanggal());
                    pstmt.setInt(3, n.getPelanggan().getId());
                    pstmt.executeUpdate();
                    try (ResultSet keys = pstmt.getGeneratedKeys()) {
                        keys.next();
                        notaId = keys.getInt(1);
                    }
                }
                insertItems(conn, notaId, n.getItems());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateNota(Nota n, int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE \"Nota\" SET nomor_nota=?, tanggal=?, pelanggan_id=? " +
                        "WHERE id=?")) {
                    pstmt.setString(1, n.getNomorNota());
                    pstmt.setObject(2, n.getTanggal());
                    pstmt.setInt(3, n.getPelanggan().getId());
                    pstmt.setInt(4, id);
                    pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM \"NotaItem\" WHERE nota_id=?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }
                insertItems(conn, id, n.getItems());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void insertItems(Connection conn, int notaId,
                             ObservableList<NotaItem> items) throws SQLException {
        String sql = "INSERT INTO \"NotaItem\" (nota_id, cetakan_id, jumlah) " +
                     "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (NotaItem item : items) {
                pstmt.setInt(1, notaId);
                pstmt.setInt(2, item.getCetakan().getId());
                pstmt.setInt(3, item.getJumlah());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void deleteNota(int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM \"Nota\" WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
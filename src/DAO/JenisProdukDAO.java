/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package DAO;
import Service.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class JenisProdukDAO {
    // Harga per kg (hardcode sesuai keputusan)
    private static double hargaPerKg(String kategoriLower) {
        return switch (kategoriLower) {
            case "fresh" -> 10_000.0;
            case "kompos" -> 5_000.0;
            default -> 0.0;
        };
    }

    /** Tambah baris baru ke jenis_produk. Wilayah diisi dari kepala. */
    public int tambahProdukBaru(
            String kategori, int jumlahKg, 
            String wilayahKepala, String tglDibuat, String tglKadaluwarsa
    ) throws SQLException {

        if (kategori == null) throw new SQLException("Kategori wajib diisi");
        String k = kategori.trim().toLowerCase();
        if (!(k.equals("fresh") || k.equals("kompos"))) {
            throw new SQLException("Kategori tidak valid (hanya fresh/kompos)");
        }
        if (jumlahKg < 5 || jumlahKg > 100) {
            throw new SQLException("Jumlah harus 5–100 kg");
        }
        if (wilayahKepala == null || wilayahKepala.isBlank()) {
            throw new SQLException("Wilayah kepala tidak diketahui");
        }
        // Validasi tanggal: hanya satu yang diisi
        if ("fresh".equals(k)) {
            if (tglKadaluwarsa == null || tglKadaluwarsa.isBlank())
                throw new SQLException("Tanggal kadaluwarsa wajib untuk kategori fresh");
            if (tglDibuat != null && !tglDibuat.isBlank())
                throw new SQLException("Untuk fresh, hanya isi tanggal kadaluwarsa (tanggal dibuat harus kosong)");
        } else { // kompos
            if (tglDibuat == null || tglDibuat.isBlank())
                throw new SQLException("Tanggal dibuat wajib untuk kategori kompos");
            if (tglKadaluwarsa != null && !tglKadaluwarsa.isBlank())
                throw new SQLException("Untuk kompos, hanya isi tanggal dibuat (tanggal kadaluwarsa harus kosong)");
        }

        double totalHarga = jumlahKg * hargaPerKg(k);

        String sql = """
            INSERT INTO jenis_produk
              (kategori_produk, jumlah_produk, total_harga, wilayah, tanggal_kadaluwarsa, tanggal_dibuat)
            VALUES (?,?,?,?,?,?)
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, k);                 // kategori_produk
            ps.setInt(2, jumlahKg);             // jumlah_produk
            ps.setDouble(3, totalHarga);        // total_harga
            ps.setString(4, wilayahKepala);     // wilayah

            // tanggal_kadaluwarsa & tanggal_dibuat (DATE) — bisa null salah satunya
            if (tglKadaluwarsa == null || tglKadaluwarsa.isBlank()) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(tglKadaluwarsa)); // yyyy-MM-dd
            }

            if (tglDibuat == null || tglDibuat.isBlank()) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(tglDibuat)); // yyyy-MM-dd
            }

            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Gagal menambah produk");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new SQLException("Gagal mengambil ID produk baru");
        }
    }

    /** Hapus berdasarkan id_jenis_produk — ditolak jika sudah dipakai di transaksi. */
    public boolean hapusProdukJikaBoleh(int idProduk, String wilayahKepala) throws SQLException {
        // 1) Cek eksistensi & wilayah
        String cekSql = "SELECT wilayah FROM jenis_produk WHERE id_jenis_produk = ?";
        // 2) Cek referensi transaksi
        String refSql = "SELECT COUNT(*) FROM transaksi WHERE id_jenis_produk = ?";
        // 3) Hapus
        String delSql = "DELETE FROM jenis_produk WHERE id_jenis_produk = ?";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String wilayah;
                try (PreparedStatement ps = c.prepareStatement(cekSql)) {
                    ps.setInt(1, idProduk);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false; // ID tidak ditemukan
                        }
                        wilayah = rs.getString(1);
                    }
                }
                if (wilayah == null || !wilayah.equalsIgnoreCase(wilayahKepala)) {
                    c.rollback();
                    throw new SQLException("Anda tidak berhak menghapus produk di wilayah berbeda");
                }

                long dipakai;
                try (PreparedStatement ps = c.prepareStatement(refSql)) {
                    ps.setInt(1, idProduk);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        dipakai = rs.getLong(1);
                    }
                }
                if (dipakai > 0) {
                    c.rollback();
                    throw new SQLException("Produk sudah dipakai di transaksi — tidak dapat dihapus");
                }

                int rows;
                try (PreparedStatement ps = c.prepareStatement(delSql)) {
                    ps.setInt(1, idProduk);
                    rows = ps.executeUpdate();
                }
                if (rows == 0) {
                    c.rollback();
                    return false; // jaga-jaga
                }

                c.commit();
                return true;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
    
        /** Ambil wilayah yang dikelola oleh kepala (id_user) */
    public String ambilWilayahKepala(int idUserKepala) throws SQLException {
        String sql = "SELECT wilayah_dikelola FROM kepala_administrasi WHERE id_user = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUserKepala);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
                throw new SQLException("Wilayah kepala tidak ditemukan");
            }
        }
    }

    /** Baris data mentah jenis_produk (DTO kecil untuk DAO) */
    public static record JenisProdukRow(
        int idJenisProduk, String kategori, int jumlahKg,
        double totalHarga, String wilayah, Date tglKadaluarsa, Date tglDibuat
    ) {}

    /** Ambil semua produk untuk 1 wilayah (untuk isi JTable) */
    public List<JenisProdukRow> ambilSemuaProdukByWilayah(String wilayah) throws SQLException {
        String sql = """
            SELECT id_jenis_produk, kategori_produk, jumlah_produk, total_harga,
                   wilayah, tanggal_kadaluwarsa, tanggal_dibuat
            FROM jenis_produk
            WHERE wilayah = ?
            ORDER BY id_jenis_produk DESC
        """;
        List<JenisProdukRow> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new JenisProdukRow(
                        rs.getInt(1), rs.getString(2),
                        rs.getInt(3), rs.getDouble(4),
                        rs.getString(5), rs.getDate(6), rs.getDate(7)
                    ));
                }
            }
        }
        return out;
    }
    
    public boolean updateProduk(
            int idProduk, String kategori, int jumlahKg,
            String wilayahKepala, String tglDibuat, String tglKadaluwarsa
    ) throws SQLException {

        // Validasi input (mirip seperti tambahProdukBaru)
        if (kategori == null) throw new SQLException("Kategori wajib diisi");
        String k = kategori.trim().toLowerCase();
        if (!(k.equals("fresh") || k.equals("kompos"))) {
            throw new SQLException("Kategori tidak valid (hanya fresh/kompos)");
        }
        if (jumlahKg < 5 || jumlahKg > 100) {
            throw new SQLException("Jumlah harus 5–100 kg");
        }
        if ("fresh".equals(k)) {
            if (tglKadaluwarsa == null || tglKadaluwarsa.isBlank())
                throw new SQLException("Tanggal kadaluwarsa wajib untuk kategori fresh");
            if (tglDibuat != null && !tglDibuat.isBlank())
                throw new SQLException("Untuk fresh, hanya isi tanggal kadaluwarsa");
        } else { // kompos
            if (tglDibuat == null || tglDibuat.isBlank())
                throw new SQLException("Tanggal dibuat wajib untuk kategori kompos");
            if (tglKadaluwarsa != null && !tglKadaluwarsa.isBlank())
                throw new SQLException("Untuk kompos, hanya isi tanggal dibuat");
        }

        // Hitung ulang total harga berdasarkan aturan bisnis
        double totalHarga = jumlahKg * hargaPerKg(k);

        String sql = """
            UPDATE jenis_produk SET
                kategori_produk = ?,
                jumlah_produk = ?,
                total_harga = ?,
                tanggal_kadaluwarsa = ?,
                tanggal_dibuat = ?
            WHERE
                id_jenis_produk = ? AND wilayah = ?
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, k);
            ps.setInt(2, jumlahKg);
            ps.setDouble(3, totalHarga);

            // Set tanggal kadaluwarsa (bisa null)
            if (tglKadaluwarsa == null || tglKadaluwarsa.isBlank()) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(tglKadaluwarsa));
            }

            // Set tanggal dibuat (bisa null)
            if (tglDibuat == null || tglDibuat.isBlank()) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(tglDibuat));
            }

            // Parameter untuk klausa WHERE (keamanan)
            ps.setInt(6, idProduk);
            ps.setString(7, wilayahKepala);

            int affectedRows = ps.executeUpdate();
            
            // Jika affectedRows > 0, berarti update berhasil.
            // Jika 0, berarti tidak ada produk dengan ID tersebut di wilayah kepala.
            return affectedRows > 0;
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import Service.DBConnection;
import Model.KeranjangItem; 
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zyrus
 */
public class PelangganDAO {

public record BarisProdukToko(
        int idJenisProduk, String kategori, int jumlahKg, double totalHarga,
        String wilayah, Date tanggalKadaluwarsa, Date tanggalDibuat
    ) {}
    
    public record HargaProduk(
        String kategori, double hargaPerKg, int stokTersedia, String wilayahKepala
    ) {}

    public record TransaksiHistoryRow(
        int idTransaksi, String jenisProduk, int jumlahProduk, double totalHarga, Date tanggalTransaksi
    ) {}

    // Harga per kg (hardcode sesuai keputusan) - Disimpan di DAO untuk perhitungan harga
    private static double hargaPerKg(String kategoriLower) {
        return switch (kategoriLower) {
            case "fresh" -> 10_000.0;
            case "kompos" -> 5_000.0;
            default -> 0.0;
        };
    }

    // --- Metode Ambil Produk Tersedia, getDetailProduk, getIdKepalaByWilayah (Sudah OK) ---
    public List<BarisProdukToko> ambilProdukTersedia(String wilayah, String kategori) throws SQLException {
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String sql = "SELECT id_jenis_produk, kategori_produk, jumlah_produk, total_harga, wilayah, tanggal_kadaluwarsa, tanggal_dibuat FROM jenis_produk WHERE jumlah_produk > 0";

        if (wilayah != null && !wilayah.isEmpty()) {
            conditions.add("wilayah = ?");
            params.add(wilayah);
        }
        if (kategori != null && !kategori.isEmpty()) {
            conditions.add("kategori_produk = ?");
            params.add(kategori);
        }

        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }
        sql += " ORDER BY tanggal_kadaluwarsa ASC, id_jenis_produk DESC";


        List<BarisProdukToko> hasil = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisProdukToko(
                        rs.getInt("id_jenis_produk"),
                        rs.getString("kategori_produk"),
                        rs.getInt("jumlah_produk"),
                        rs.getDouble("total_harga"),
                        rs.getString("wilayah"),
                        rs.getDate("tanggal_kadaluwarsa"),
                        rs.getDate("tanggal_dibuat")
                    ));
                }
            }
        }
        return hasil;
    }
    
    public HargaProduk getDetailProduk(int idJenisProduk) throws SQLException {
        String sql = "SELECT kategori_produk, jumlah_produk, wilayah FROM jenis_produk WHERE id_jenis_produk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idJenisProduk);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Produk ID " + idJenisProduk + " tidak ditemukan.");
                
                String kategori = rs.getString("kategori_produk");
                double harga = hargaPerKg(kategori.toLowerCase());
                int stok = rs.getInt("jumlah_produk");
                String wilayah = rs.getString("wilayah");
                
                return new HargaProduk(kategori, harga, stok, wilayah);
            }
        }
    }
    
    public int getIdKepalaByWilayah(String wilayah) throws SQLException {
        String sql = "SELECT id_user FROM kepala_administrasi WHERE wilayah_dikelola = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 3; 
            }
        }
    }

    /**
     * Memproses INSERT transaksi dan UPDATE stok produk (transaksi database).
     * [PERBAIKAN] Mengembalikan ID Transaksi.
     */
    public int prosesTransaksi(int idPelanggan, KeranjangItem item) throws Exception {
        
        // GUNAKAN GETTER
        HargaProduk detail = getDetailProduk(item.getIdJenisProduk());
        
        if (item.getJumlahKg() > detail.stokTersedia) {
            throw new Exception("Stok tidak mencukupi untuk produk ID " + item.getIdJenisProduk() + 
                                " (Butuh: " + item.getJumlahKg() + "kg, Tersedia: " + detail.stokTersedia + "kg)");
        }
        
        int idKepala = getIdKepalaByWilayah(detail.wilayahKepala());
        double totalHarga = item.getJumlahKg() * detail.hargaPerKg();
        int idTransaksi = -1; // Variabel untuk menampung ID

        String insertSql = "INSERT INTO transaksi (id_user_pelanggan, id_user_kepala, id_jenis_produk, jenis_produk, tanggal_transaksi, total_harga, jumlah_produk) VALUES (?, ?, ?, ?, NOW(), ?, ?)";
        String updateSql = "UPDATE jenis_produk SET jumlah_produk = jumlah_produk - ? WHERE id_jenis_produk = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. INSERT Transaksi dan Ambil ID yang di-generate
                try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idPelanggan);
                    ps.setInt(2, idKepala);
                    ps.setInt(3, item.getIdJenisProduk()); // GUNAKAN GETTER
                    ps.setString(4, item.getKategori());    // GUNAKAN GETTER
                    ps.setDouble(5, totalHarga);
                    ps.setDouble(6, item.getJumlahKg()); // GUNAKAN GETTER
                    ps.executeUpdate();
                    
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            idTransaksi = rs.getInt(1); 
                        }
                    }
                }

                // 2. UPDATE Stok
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, item.getJumlahKg()); // GUNAKAN GETTER
                    ps.setInt(2, item.getIdJenisProduk()); // GUNAKAN GETTER
                    ps.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new Exception("Transaksi dibatalkan karena error database: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        }
        
        if (idTransaksi == -1) {
             throw new Exception("Gagal mendapatkan ID Transaksi yang baru.");
        }
        return idTransaksi; // KEMBALIKAN ID TRANSAKSI
    }
    
    /**
     * Mengambil riwayat transaksi Pelanggan. (Sudah OK)
     */
    public List<TransaksiHistoryRow> getHistoryPembelian(int idPelanggan) throws SQLException {
        String sql = "SELECT id_transaksi, jenis_produk, jumlah_produk, total_harga, tanggal_transaksi FROM transaksi WHERE id_user_pelanggan = ? ORDER BY tanggal_transaksi DESC";
        
        List<TransaksiHistoryRow> history = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new TransaksiHistoryRow(
                        rs.getInt("id_transaksi"),
                        rs.getString("jenis_produk"),
                        rs.getInt("jumlah_produk"),
                        rs.getDouble("total_harga"),
                        rs.getDate("tanggal_transaksi")
                    ));
                }
            }
        }
        return history;
    }
}
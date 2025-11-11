/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import Service.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types; // Pastikan import ini ada
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class StafDAO implements IStafDAO {

    public record BarisProduksi(
        int idProduksi,
        int idHotel,
        String kategori,
        int totalProduksi,
        Date tanggalProduksi
    ) {}

    public record BarisKonsumsi(
        int idKonsumsi,
        int idProduksi,
        int totalTermakan,
        String kategori,
        Date tanggalKonsumsi
    ) {}
    
    public record BarisBahanBaku(
        int idBahanBaku,
        String nama,
        String jenis,
        int jumlah,
        Date tanggalKadaluwarsa,
        int hariTersisa,
        String kategoriKadaluwarsa
    ) {}
    
    public record BarisSisaPangan(
        int idSisaPangan,
        int idKonsumsi,
        int idBahanBaku,
        String kategori,
        int totalSisaPangan,
        Date tanggal,
        String namaHotel
    ) {}
    
    public record PilihanID(
        int id,
        String deskripsi,
        String kategori
    ) {}


    // (Metode Anda: ambilWilayahStaf, ambilProduksiBerdasarkanWilayah, ambilKonsumsiBerdasarkanWilayah, ambilSemuaBahanBaku)
    // ... (kode metode Anda yang sudah ada di sini) ...
     public String ambilWilayahStaf(int idPenggunaStaf) throws SQLException {
        String sql = "SELECT wilayah_dikelola FROM staff_administrasi WHERE id_user = ?";
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            
            ps.setInt(1, idPenggunaStaf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("wilayah_dikelola");
                } else {
                    throw new SQLException("Staf dengan ID " + idPenggunaStaf + " tidak ditemukan.");
                }
            }
        }
    }

    public List<BarisProduksi> ambilProduksiBerdasarkanWilayah(String wilayah) throws SQLException {
        String sql = "SELECT p.id_produksi, p.id_hotel, p.kategori_produksi, p.total_produksi, p.tanggal_produksi " +
                     "FROM produksi p JOIN hotel h ON p.id_hotel = h.id_hotel " +
                     "WHERE h.wilayah = ? ORDER BY p.tanggal_produksi DESC, p.id_produksi DESC";

        List<BarisProduksi> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisProduksi(
                        rs.getInt("id_produksi"),
                        rs.getInt("id_hotel"),
                        rs.getString("kategori_produksi"),
                        rs.getInt("total_produksi"),
                        rs.getDate("tanggal_produksi")
                    ));
                }
            }
        }
        return hasil;
    }

    public List<BarisKonsumsi> ambilKonsumsiBerdasarkanWilayah(String wilayah) throws SQLException {
        String sql = "SELECT k.id_konsumsi, k.id_produksi, k.total_termakan, k.kategori_konsumsi, p.tanggal_produksi AS tanggal_konsumsi " +
                     "FROM konsumsi k " +
                     "JOIN produksi p ON k.id_produksi = p.id_produksi " +
                     "JOIN hotel h ON p.id_hotel = h.id_hotel " +
                     "WHERE h.wilayah = ? ORDER BY p.tanggal_produksi DESC, k.id_konsumsi DESC";

        List<BarisKonsumsi> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisKonsumsi(
                        rs.getInt("id_konsumsi"),
                        rs.getInt("id_produksi"),
                        rs.getInt("total_termakan"),
                        rs.getString("kategori_konsumsi"),
                        rs.getDate("tanggal_konsumsi")
                    ));
                }
            }
        }
        return hasil;
    }

    public List<BarisBahanBaku> ambilSemuaBahanBaku() throws SQLException {
        String sql = "SELECT id_bahan_baku, nama_bahan_baku, jenis_bahan_baku, " +
                     "jumlah_bahan_baku, tanggal_kadaluwarsa, " + 
                     "jumlah_hari_tersisa, kategori_kadaluwarsa " +
                     "FROM bahan_baku ORDER BY tanggal_kadaluwarsa ASC";
        
        List<BarisBahanBaku> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                hasil.add(new BarisBahanBaku(
                    rs.getInt("id_bahan_baku"),
                    rs.getString("nama_bahan_baku"),
                    rs.getString("jenis_bahan_baku"),
                    rs.getInt("jumlah_bahan_baku"),
                    rs.getDate("tanggal_kadaluwarsa"),
                    rs.getInt("jumlah_hari_tersisa"),
                    rs.getString("kategori_kadaluwarsa")
                ));
            }
        }
        return hasil;
    }

    // (Metode Sisa Pangan Anda: ambilSisaPanganBerdasarkanWilayah, ambilKonsumsiAktif, ambilBahanBakuAktif, tambahSisaPangan, hapusSisaPangan)
    // ... (kode metode sisa pangan Anda yang sudah ada di sini) ...
    
    public List<BarisSisaPangan> ambilSisaPanganBerdasarkanWilayah(String wilayah) throws SQLException {
        String sql = """
            SELECT 
                s.id_sisa_pangan, s.id_konsumsi, s.id_bahan_baku, s.kategori_sisa_pangan,
                s.total_sisa_pangan, s.tanggal_sisa_pangan, 
                COALESCE(h.nama_hotel, 'Gudang') AS nama_hotel
            FROM sisa_pangan s
            JOIN staff_administrasi sa ON s.id_user = sa.id_user
            LEFT JOIN konsumsi k ON s.id_konsumsi = k.id_konsumsi
            LEFT JOIN produksi p ON k.id_produksi = p.id_produksi
            LEFT JOIN hotel h ON p.id_hotel = h.id_hotel
            WHERE sa.wilayah_dikelola = ?
            ORDER BY s.tanggal_sisa_pangan DESC
            """;
        
        List<BarisSisaPangan> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BarisSisaPangan(
                        rs.getInt("id_sisa_pangan"),
                        rs.getInt("id_konsumsi"),
                        rs.getInt("id_bahan_baku"),
                        rs.getString("kategori_sisa_pangan"),
                        rs.getInt("total_sisa_pangan"),
                        rs.getDate("tanggal_sisa_pangan"),
                        rs.getString("nama_hotel")
                    ));
                }
            }
        }
        return hasil;
    }
    
    public List<PilihanID> ambilKonsumsiAktif(String wilayah) throws SQLException {
        String sql = """
            SELECT k.id_konsumsi, k.kategori_konsumsi 
            FROM konsumsi k
            JOIN produksi p ON k.id_produksi = p.id_produksi
            JOIN hotel h ON p.id_hotel = h.id_hotel
            WHERE h.wilayah = ?
            ORDER BY k.id_konsumsi DESC
            """;
        List<PilihanID> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, wilayah);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new PilihanID(
                        rs.getInt("id_konsumsi"),
                        "", 
                        rs.getString("kategori_konsumsi")
                    ));
                }
            }
        }
        return hasil;
    }
    
    public List<PilihanID> ambilBahanBakuAktif() throws SQLException {
        String sql = "SELECT id_bahan_baku, nama_bahan_baku, jenis_bahan_baku FROM bahan_baku WHERE jumlah_bahan_baku > 0 ORDER BY nama_bahan_baku";
        List<PilihanID> hasil = new ArrayList<>();
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                hasil.add(new PilihanID(
                    rs.getInt("id_bahan_baku"),
                    rs.getString("nama_bahan_baku"),
                    rs.getString("jenis_bahan_baku")
                ));
            }
        }
        return hasil;
    }

    public void tambahSisaPangan(int idPenggunaStaf, int idSumber, String tipeSumber, 
                                String kategori, int totalKg, String tanggalYmd) throws SQLException {
        
        String sql = """
            INSERT INTO sisa_pangan 
                (id_konsumsi, id_bahan_baku, id_user, kategori_sisa_pangan, total_sisa_pangan, tanggal_sisa_pangan) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {

            if (tipeSumber.equalsIgnoreCase("Konsumsi")) {
                ps.setInt(1, idSumber);
                ps.setNull(2, Types.INTEGER);
            } else { // Bahan Baku
                ps.setNull(1, Types.INTEGER);
                ps.setInt(2, idSumber);
            }
            
            ps.setInt(3, idPenggunaStaf);
            ps.setString(4, kategori);
            ps.setInt(5, totalKg);
            ps.setDate(6, Date.valueOf(tanggalYmd));
            
            ps.executeUpdate();
        }
    }

    public boolean hapusSisaPangan(int idSisaPangan, String wilayahStaf) throws SQLException {
        String sql = """
            DELETE s FROM sisa_pangan s
            JOIN staff_administrasi sa ON s.id_user = sa.id_user
            WHERE s.id_sisa_pangan = ? AND sa.wilayah_dikelola = ?
            """;
        
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {
            
            ps.setInt(1, idSisaPangan);
            ps.setString(2, wilayahStaf);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * [DIUBAH] Mengupdate data sisa pangan (hanya total dan tanggal).
     * Sumber data (id_konsumsi, id_bahan_baku, kategori) tidak diubah.
     */
    public boolean updateSisaPangan(
            int idSisaPanganTarget, int totalKgBaru, String tanggalYmdBaru,
            String wilayahStaf) throws SQLException {

        // Kueri UPDATE ini melakukan JOIN dengan staff_administrasi
        // untuk memvalidasi wilayah sebelum mengupdate.
        String sql = """
            UPDATE sisa_pangan s
            JOIN staff_administrasi sa ON s.id_user = sa.id_user
            SET
                s.total_sisa_pangan = ?,
                s.tanggal_sisa_pangan = ?
            WHERE
                s.id_sisa_pangan = ? AND sa.wilayah_dikelola = ?
            """;
        
        try (Connection koneksi = DBConnection.getConnection();
             PreparedStatement ps = koneksi.prepareStatement(sql)) {

            // === Bagian SET ===
            ps.setInt(1, totalKgBaru);
            ps.setDate(2, Date.valueOf(tanggalYmdBaru));
            
            // === Bagian WHERE (Keamanan) ===
            ps.setInt(3, idSisaPanganTarget);
            ps.setString(4, wilayahStaf);

            int affectedRows = ps.executeUpdate();
            
            // Mengembalikan true jika 1 baris berhasil diupdate
            return affectedRows > 0;
        }
    }
}
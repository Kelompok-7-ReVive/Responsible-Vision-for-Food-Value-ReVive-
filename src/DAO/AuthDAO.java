/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import Model.UserAccount;
import Service.DBConnection;
import Model.Kepala; // Import kelas Model baru
import Model.Staf;   // Import kelas Model baru
import Model.Pelanggan; // Import kelas Model baru
import java.sql.*;
import java.util.Optional;
/**
 *
 * @author Zyrus
 */
public class AuthDAO {
    
    public Optional<UserAccount> login(String email, String password) {
        String sql = "SELECT id_user, id_hotel, nama, email, password, role FROM user WHERE BINARY email=? AND BINARY password=?";
        
        try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty(); // Tidak ditemukan

                // Ambil data dasar
                int idUser = rs.getInt("id_user");
                int idHotel = rs.getInt("id_hotel");
                String nama = rs.getString("nama");
                String role = rs.getString("role");
                
                // Ambil password lagi untuk memastikan konsistensi dengan model
                String dbPassword = rs.getString("password"); 
                
                // 1. Cek role dan ambil data unik (wilayah)
                String wilayahDikelola = null;
                
                if ("Kepala_administrasi".equalsIgnoreCase(role) || "Staff_administrasi".equalsIgnoreCase(role)) {
                    wilayahDikelola = ambilWilayahTambahan(c, idUser, role);
                }
                
                // 2. [POLIMORFISME] Instansiasi kelas konkret yang benar
                UserAccount user;
                if ("Kepala_administrasi".equalsIgnoreCase(role)) {
                    // Kepala tidak perlu idHotel
                    user = new Kepala(idUser, nama, email, dbPassword, wilayahDikelola);
                } else if ("Staff_administrasi".equalsIgnoreCase(role)) {
                    user = new Staf(idUser, nama, email, dbPassword, wilayahDikelola, idHotel);
                } else if ("Pelanggan".equalsIgnoreCase(role)) {
                    // Untuk Pelanggan, kita butuh data mitra (jika ada)
                    String mitra = ambilMitraPelanggan(c, idUser);
                    user = new Pelanggan(idUser, nama, email, dbPassword, mitra);
                } else {
                    return Optional.empty(); // Role tidak dikenal
                }
                
                return Optional.of(user);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Helper untuk mengambil wilayah_dikelola untuk Kepala atau Staf.
     */
    private String ambilWilayahTambahan(Connection conn, int idUser, String role) throws SQLException {
        String tabel = role.equalsIgnoreCase("Kepala_administrasi") ? "kepala_administrasi" : "staff_administrasi";
        String sql = "SELECT wilayah_dikelola FROM " + tabel + " WHERE id_user = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }
    
    /**
     * Helper untuk mengambil mitra pelanggan.
     */
    private String ambilMitraPelanggan(Connection conn, int idUser) throws SQLException {
        String sql = "SELECT mitra FROM pelanggan WHERE id_user = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }
}
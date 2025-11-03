/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author Zyrus
 */
public class RegisterService {
    private static final int MIN_LEN = 3;
    private static final int MAX_LEN = 50;
    
    public void registerPelanggan(String nama, String mitra, String email, String password) throws Exception {
        // 1) Validasi basic
        validateField("Nama", nama);
        validateField("Mitra", mitra);
        validateEmail(email);
        validatePassword(password);

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Koneksi database null.");
            }
            conn.setAutoCommit(false);

            try {
                // 2) Cek email unik di tabel user
                if (emailExists(conn, email)) {
                    throw new IllegalArgumentException("Email sudah terdaftar. Gunakan email lain.");
                }

                // 3) Insert ke user (role fixed Pelanggan, id_hotel NULL)
                int idUser = insertUser(conn, nama, email, password);

                // 4) Insert ke pelanggan (subtype)
                insertPelanggan(conn, idUser, nama, email, password, mitra);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void validateField(String label, String val) {
        if (val == null) throw new IllegalArgumentException(label + " wajib diisi");
        String v = val.trim();
        if (v.length() < MIN_LEN || v.length() > MAX_LEN) {
            throw new IllegalArgumentException(label + " harus " + MIN_LEN + "–" + MAX_LEN + " karakter");
        }
    }

    private void validateEmail(String email) {
        validateField("Email", email);
        // regex sederhana
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Format email tidak valid");
        }
    }

    private void validatePassword(String password) {
        validateField("Password", password);
        // bisa tambah aturan lain kalau mau (angka, huruf besar, dll)
    }

    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int insertUser(Connection conn, String nama, String email, String password) throws SQLException {
        String sql = "INSERT INTO user (id_hotel, nama, email, password, role) VALUES (NULL, ?, ?, ?, 'Pelanggan')";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama.trim());
            ps.setString(2, email.trim());
            ps.setString(3, password); // NOTE: plain text; nanti bisa di-hash
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Gagal membuat user (id tidak dihasilkan).");
    }

    private void insertPelanggan(Connection conn, int idUser, String nama, String email, String password, String mitra) throws SQLException {
        String sql = "INSERT INTO pelanggan (id_user, nama, email, password, mitra) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setString(2, nama.trim());
            ps.setString(3, email.trim());
            ps.setString(4, password);
            ps.setString(5, mitra.trim());
            ps.executeUpdate();
        }
    }
}
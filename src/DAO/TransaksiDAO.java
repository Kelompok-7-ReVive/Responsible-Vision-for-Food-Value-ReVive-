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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class TransaksiDAO {
    public record TransaksiRow(
        int idTransaksi,
        String namaPelanggan,
        int idJenisProduk,
        String jenisProduk,
        Date tanggalTransaksi,
        int jumlahProduk,
        double totalHarga
    ) {}
    
    public List<TransaksiRow> getTransaksiForKepala(int idUserKepala) throws SQLException {
        String sql = """
            SELECT
                t.id_transaksi,
                p.nama AS nama_pelanggan,
                t.id_jenis_produk,
                t.jenis_produk,
                t.tanggal_transaksi,
                t.jumlah_produk,
                t.total_harga
            FROM transaksi t
            JOIN pelanggan p ON t.id_user_pelanggan = p.id_user
            WHERE t.id_user_kepala = ?
            ORDER BY t.tanggal_transaksi DESC, t.id_transaksi DESC
            """;

        List<TransaksiRow> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUserKepala);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new TransaksiRow(
                        rs.getInt("id_transaksi"),
                        rs.getString("nama_pelanggan"),
                        rs.getInt("id_jenis_produk"),
                        rs.getString("jenis_produk"),
                        rs.getDate("tanggal_transaksi"),
                        rs.getInt("jumlah_produk"),
                        rs.getDouble("total_harga")
                    ));
                }
            }
        }
        return results;
    }
}

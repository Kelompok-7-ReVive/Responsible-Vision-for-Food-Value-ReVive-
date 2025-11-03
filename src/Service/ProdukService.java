/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.JenisProdukDAO;
import DAO.JenisProdukDAO.JenisProdukRow;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class ProdukService {
    private final JenisProdukDAO dao = new JenisProdukDAO();

    // === DTO & ENUM yang dipakai UI ===
    public record BarisProduk(
        int idJenisProduk, String kategori, int jumlahKg,
        double totalHarga, String wilayah,
        String tanggalKadaluwarsa, String tanggalDibuat
    ) {}

    public enum HasilHapus { BERHASIL, SUDAH_ADA_TRANSAKSI, TIDAK_DITEMUKAN, TIDAK_BERWENANG }

    // === UTIL FORMAT TANGGAL (yyyy-MM-dd atau "-") ===
    private static String fmt(java.sql.Date d) {
        return d == null ? "-" : d.toString(); // java.sql.Date#toString => yyyy-MM-dd
    }

    // === 1) LIST DATA UNTUK TABEL ===
    public List<BarisProduk> ambilSemuaProdukKepala(int idUserKepala) throws SQLException {
        String wilayah = dao.ambilWilayahKepala(idUserKepala);
        List<JenisProdukRow> rows = dao.ambilSemuaProdukByWilayah(wilayah);
        List<BarisProduk> out = new ArrayList<>(rows.size());
        for (JenisProdukRow r : rows) {
            out.add(new BarisProduk(
                r.idJenisProduk(), r.kategori(), r.jumlahKg(),
                r.totalHarga(), r.wilayah(),
                fmt(r.tglKadaluarsa()), fmt(r.tglDibuat())
            ));
        }
        return out;
    }

    // === 2) TAMBAH PRODUK BARU ===
    public void tambahProdukBaru(int idUserKepala, String kategoriLower, int jumlahKg,
                                 String tanggalKadaluwarsaYmdOrNull, String tanggalDibuatYmdOrNull,
                                 double totalHargaDariUI_Abaikan) throws SQLException {
        // Catatan: total harga dihitung ulang di DAO (sesuai aturan),
        // parameter total dari UI diabaikan supaya aman.
        String wilayah = dao.ambilWilayahKepala(idUserKepala);
        dao.tambahProdukBaru(
            kategoriLower, jumlahKg,
            wilayah, // wilayah kepala
            tanggalDibuatYmdOrNull, tanggalKadaluwarsaYmdOrNull
        );
    }

    // === 3) HAPUS PRODUK DENGAN SYARAT ===
    public HasilHapus hapusProdukJikaMemenuhiSyarat(int idUserKepala, int idJenisProduk) {
        try {
            String wilayah = dao.ambilWilayahKepala(idUserKepala);
            boolean ok = dao.hapusProdukJikaBoleh(idJenisProduk, wilayah);
            return ok ? HasilHapus.BERHASIL : HasilHapus.TIDAK_DITEMUKAN;
        } catch (SQLException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("transaksi")) return HasilHapus.SUDAH_ADA_TRANSAKSI;
            if (msg.contains("berhak") || msg.contains("wilayah")) return HasilHapus.TIDAK_BERWENANG;
            // default konservatif
            return HasilHapus.TIDAK_DITEMUKAN;
        }
    }
    
    // === 4) UPDATE PRODUK ===
    public boolean updateProduk(
            int idUserKepala, int idProduk, String kategoriLower, int jumlahKg,
            String tanggalKadaluwarsaYmdOrNull, String tanggalDibuatYmdOrNull
    ) throws SQLException {
        
        // Ambil wilayah kepala untuk otorisasi (memastikan dia hanya bisa update produk di wilayahnya)
        String wilayahKepala = dao.ambilWilayahKepala(idUserKepala);
        
        // Panggil metode update di DAO
        // (Metode ini akan kita buat di JenisProdukDAO.java selanjutnya)
        return dao.updateProduk(
            idProduk,
            kategoriLower,
            jumlahKg,
            wilayahKepala, // Digunakan di klausa WHERE untuk keamanan
            tanggalDibuatYmdOrNull,
            tanggalKadaluwarsaYmdOrNull
        );
    } 
}
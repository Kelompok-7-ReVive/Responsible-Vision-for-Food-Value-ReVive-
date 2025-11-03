/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.StaffDAO;
import DAO.StaffDAO.BarisBahanBaku;
import DAO.StaffDAO.BarisKonsumsi;
import DAO.StaffDAO.BarisProduksi;
import DAO.StaffDAO.BarisSisaPangan;
import DAO.StaffDAO.PilihanID; // [BARU] Impor DTO untuk dropdown
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class LayananStaff {
    private final StaffDAO dao = new StaffDAO();

// DTO untuk data Produksi
    public record BarisProduksiUntukTabel(
        int idProduksi,
        int idHotel,
        String kategori,
        int totalProduksi,
        String tanggalProduksi
    ) {}

    // DTO untuk data Konsumsi
    public record BarisKonsumsiUntukTabel(
        int idKonsumsi,
        int idProduksi,
        int totalTermakan,
        String kategori,
        String tanggalKonsumsi
    ) {}
    
    // DTO untuk data Bahan Baku
    public record BarisBahanBakuUntukTabel(
        int idBahanBaku,
        String nama,
        String jenis,
        int jumlah,
        String tanggalKadaluwarsa,
        int hariTersisa,
        String kategoriKadaluwarsa
    ) {}
    
    // [BARU] DTO untuk data Sisa Pangan
    public record BarisSisaPanganUntukTabel(
        int idSisaPangan,
        int idKonsumsi,
        int idBahanBaku,
        String kategori,
        int totalSisaPangan,
        String tanggal,
        String namaHotel // Kolom tambahan untuk info
    ) {}

    // =================================================================
    // =================== METODE PENGAMBILAN DATA =====================
    // =================================================================

    /**
     * Mengambil dan memformat data produksi untuk seorang staf.
     */
    public List<BarisProduksiUntukTabel> ambilDataProduksiUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StaffDAO.BarisProduksi> dataMentah = dao.ambilProduksiBerdasarkanWilayah(wilayah);
        
        List<BarisProduksiUntukTabel> dataUntukUI = new ArrayList<>();
        for (StaffDAO.BarisProduksi baris : dataMentah) {
            dataUntukUI.add(new BarisProduksiUntukTabel(
                baris.idProduksi(),
                baris.idHotel(),
                baris.kategori(),
                baris.totalProduksi(),
                baris.tanggalProduksi().toString()
            ));
        }
        return dataUntukUI;
    }

    /**
     * Mengambil dan memformat data konsumsi untuk seorang staf.
     */
    public List<BarisKonsumsiUntukTabel> ambilDataKonsumsiUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StaffDAO.BarisKonsumsi> dataMentah = dao.ambilKonsumsiBerdasarkanWilayah(wilayah);
        
        List<BarisKonsumsiUntukTabel> dataUntukUI = new ArrayList<>();
        for (StaffDAO.BarisKonsumsi baris : dataMentah) {
            dataUntukUI.add(new BarisKonsumsiUntukTabel(
                baris.idKonsumsi(),
                baris.idProduksi(),
                baris.totalTermakan(),
                baris.kategori(),
                baris.tanggalKonsumsi().toString()
            ));
        }
        return dataUntukUI;
    }

    /**
     * Mengambil dan memformat semua data bahan baku.
     */
    public List<BarisBahanBakuUntukTabel> ambilDataBahanBaku() throws SQLException {
        List<StaffDAO.BarisBahanBaku> dataMentah = dao.ambilSemuaBahanBaku();
        
        List<BarisBahanBakuUntukTabel> dataUntukUI = new ArrayList<>();
        for (StaffDAO.BarisBahanBaku baris : dataMentah) {
            dataUntukUI.add(new BarisBahanBakuUntukTabel(
                baris.idBahanBaku(),
                baris.nama(),
                baris.jenis(),
                baris.jumlah(),
                baris.tanggalKadaluwarsa().toString(),
                baris.hariTersisa(),
                baris.kategoriKadaluwarsa()
            ));
        }
        return dataUntukUI;
    }

    // =================================================================
    // ================== [BARU] LOGIKA SISA PANGAN ====================
    // =================================================================

    /**
     * [BARU] Mengambil dan memformat data sisa pangan untuk seorang staf.
     */
    public List<BarisSisaPanganUntukTabel> ambilDataSisaPanganUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StaffDAO.BarisSisaPangan> dataMentah = dao.ambilSisaPanganBerdasarkanWilayah(wilayah);

        List<BarisSisaPanganUntukTabel> dataUntukUI = new ArrayList<>();
        for (StaffDAO.BarisSisaPangan baris : dataMentah) {
            dataUntukUI.add(new BarisSisaPanganUntukTabel(
                baris.idSisaPangan(),
                baris.idKonsumsi(),
                baris.idBahanBaku(),
                baris.kategori(),
                baris.totalSisaPangan(),
                baris.tanggal().toString(),
                baris.namaHotel()
            ));
        }
        return dataUntukUI;
    }

    /**
     * [BARU] Mengambil daftar konsumsi yang valid untuk dropdown di UI.
     */
    public List<String> ambilDaftarKonsumsiTersedia(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<PilihanID> daftarPilihan = dao.ambilKonsumsiAktif(wilayah);
        
        List<String> hasilString = new ArrayList<>();
        for (PilihanID pilihan : daftarPilihan) {
            // Format: "ID 101 (Karbohidrat)"
            hasilString.add("ID " + pilihan.id() + " (" + pilihan.kategori() + ")");
        }
        return hasilString;
    }

    /**
     * [BARU] Mengambil daftar bahan baku yang valid untuk dropdown di UI.
     */
    public List<String> ambilDaftarBahanBakuTersedia() throws SQLException {
        List<PilihanID> daftarPilihan = dao.ambilBahanBakuAktif();
        
        List<String> hasilString = new ArrayList<>();
        for (PilihanID pilihan : daftarPilihan) {
            // Format: "ID 18 (Udang Segar - Lauk)"
            hasilString.add("ID " + pilihan.id() + " (" + pilihan.deskripsi() + " - " + pilihan.kategori() + ")");
        }
        return hasilString;
    }

    /**
     * [BARU] Menambahkan data sisa pangan baru.
     */
    public void tambahSisaPanganBaru(
            int idPenggunaStaf, int idSumber, String tipeSumber, 
            String kategori, int totalKg, String tanggalYmd) throws SQLException {
        
        dao.tambahSisaPangan(
            idPenggunaStaf, 
            idSumber, 
            tipeSumber, 
            kategori, 
            totalKg, 
            tanggalYmd
        );
    }

    /**
     * [BARU] Menghapus data sisa pangan, dengan validasi wilayah.
     */
    public boolean hapusSisaPangan(int idPenggunaStaf, int idSisaPangan) throws SQLException {
        String wilayahStaf = dao.ambilWilayahStaf(idPenggunaStaf);
        return dao.hapusSisaPangan(idSisaPangan, wilayahStaf);
    }
}
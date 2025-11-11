/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.StafDAO;
import DAO.StafDAO.BarisBahanBaku;
import DAO.StafDAO.BarisKonsumsi;
import DAO.StafDAO.BarisProduksi;
import DAO.StafDAO.BarisSisaPangan;
import DAO.StafDAO.PilihanID;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class LayananStaf implements ILayananStaf {
    private final StafDAO dao = new StafDAO();

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
    
    // DTO untuk data Sisa Pangan
    public record BarisSisaPanganUntukTabel(
        int idSisaPangan,
        int idKonsumsi,
        int idBahanBaku,
        String kategori,
        int totalSisaPangan,
        String tanggal,
        String namaHotel
    ) {}

    // =================================================================
    // =================== METODE PENGAMBILAN DATA =====================
    // =================================================================

    public List<BarisProduksiUntukTabel> ambilDataProduksiUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StafDAO.BarisProduksi> dataMentah = dao.ambilProduksiBerdasarkanWilayah(wilayah);
        
        List<BarisProduksiUntukTabel> dataUntukUI = new ArrayList<>();
        for (StafDAO.BarisProduksi baris : dataMentah) {
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

    public List<BarisKonsumsiUntukTabel> ambilDataKonsumsiUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StafDAO.BarisKonsumsi> dataMentah = dao.ambilKonsumsiBerdasarkanWilayah(wilayah);
        
        List<BarisKonsumsiUntukTabel> dataUntukUI = new ArrayList<>();
        for (StafDAO.BarisKonsumsi baris : dataMentah) {
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

    public List<BarisBahanBakuUntukTabel> ambilDataBahanBaku() throws SQLException {
        List<StafDAO.BarisBahanBaku> dataMentah = dao.ambilSemuaBahanBaku();
        
        List<BarisBahanBakuUntukTabel> dataUntukUI = new ArrayList<>();
        for (StafDAO.BarisBahanBaku baris : dataMentah) {
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
    // ================== LOGIKA SISA PANGAN ===========================
    // =================================================================

    public List<BarisSisaPanganUntukTabel> ambilDataSisaPanganUntukStaf(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<StafDAO.BarisSisaPangan> dataMentah = dao.ambilSisaPanganBerdasarkanWilayah(wilayah);

        List<BarisSisaPanganUntukTabel> dataUntukUI = new ArrayList<>();
        for (StafDAO.BarisSisaPangan baris : dataMentah) {
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

    public List<String> ambilDaftarKonsumsiTersedia(int idPenggunaStaf) throws SQLException {
        String wilayah = dao.ambilWilayahStaf(idPenggunaStaf);
        List<PilihanID> daftarPilihan = dao.ambilKonsumsiAktif(wilayah);
        
        List<String> hasilString = new ArrayList<>();
        for (PilihanID pilihan : daftarPilihan) {
            hasilString.add("ID " + pilihan.id() + " (" + pilihan.kategori() + ")");
        }
        return hasilString;
    }

    public List<String> ambilDaftarBahanBakuTersedia() throws SQLException {
        List<PilihanID> daftarPilihan = dao.ambilBahanBakuAktif();
        
        List<String> hasilString = new ArrayList<>();
        for (PilihanID pilihan : daftarPilihan) {
            hasilString.add("ID " + pilihan.id() + " (" + pilihan.deskripsi() + " - " + pilihan.kategori() + ")");
        }
        return hasilString;
    }

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

    public boolean hapusSisaPangan(int idPenggunaStaf, int idSisaPangan) throws SQLException {
        String wilayahStaf = dao.ambilWilayahStaf(idPenggunaStaf);
        return dao.hapusSisaPangan(idSisaPangan, wilayahStaf);
    }
    
    /**
     * [DIUBAH] Mengupdate data sisa pangan (hanya total dan tanggal).
     */
    public boolean updateSisaPangan(
            int idPenggunaStaf, int idSisaPanganTarget,
            int totalKgBaru, String tanggalYmdBaru) throws SQLException {
        
        // Ambil wilayah staf untuk otorisasi
        String wilayahStaf = dao.ambilWilayahStaf(idPenggunaStaf);
        
        // Panggil metode update di DAO (yang akan kita buat selanjutnya)
        // Perhatikan parameter yang lebih sederhana
        return dao.updateSisaPangan(
            idSisaPanganTarget,
            totalKgBaru,
            tanggalYmdBaru,
            wilayahStaf // Untuk klausa WHERE yang aman
        );
    }
}
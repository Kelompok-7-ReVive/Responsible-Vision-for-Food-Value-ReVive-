/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.PelangganDAO;
import Model.KeranjangItem; // Diperlukan untuk menggunakan getter
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 *
 * @author Zyrus
 */
public class LayananPelanggan {
    
private final PelangganDAO dao = new PelangganDAO();

    public record TransaksiResult(String invoiceText, int idTransaksi) {}
    
    public record BarisProdukToko(
        int idJenisProduk, String wilayah, String kategori, int jumlahKg,
        double hargaPerKg, String tanggalKadaluwarsa
    ) {}
    
    public record BarisHistory(
        int idTransaksi, String jenisProduk, int jumlahProduk, 
        String totalHarga, String tanggalTransaksi
    ) {}
    
    /**
     * Mengambil daftar produk yang tersedia berdasarkan filter Wilayah dan Kategori.
     */
    public List<BarisProdukToko> ambilProdukTersedia(String wilayahFilter, String kategoriFilter) throws SQLException {
        List<PelangganDAO.BarisProdukToko> rawData = dao.ambilProdukTersedia(wilayahFilter, kategoriFilter);
        
        return rawData.stream()
                .map(r -> {
                    try {
                        double hargaPerKg = dao.getDetailProduk(r.idJenisProduk()).hargaPerKg();
                        
                        return new BarisProdukToko(
                            r.idJenisProduk(),
                            r.wilayah(),
                            r.kategori(),
                            r.jumlahKg(),
                            hargaPerKg, 
                            r.tanggalKadaluwarsa() != null ? r.tanggalKadaluwarsa().toString() : "-"
                        );
                    } catch (SQLException e) {
                        return null; 
                    }
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    /**
     * Menghitung total harga untuk satu item keranjang.
     * [PERBAIKAN] Menggunakan getter.
     */
    public double hitungTotalHargaItem(int idJenisProduk, double jumlahBeli) {
        try {
            PelangganDAO.HargaProduk detail = dao.getDetailProduk(idJenisProduk);
            return jumlahBeli * detail.hargaPerKg();
        } catch (SQLException e) {
            return 0.0; 
        }
    }

    /**
     * Memproses seluruh transaksi pembelian yang ada di keranjang.
     * [PERBAIKAN] Menggunakan getter dan DTO TransaksiResult.
     */
    public TransaksiResult prosesPembelian(int idPelanggan, List<KeranjangItem> keranjang) throws Exception {
        
        StringBuilder invoiceDetail = new StringBuilder();
        double totalAkhir = 0;
        int lastIdTransaksi = 0;
        
        if (keranjang == null || keranjang.isEmpty()) {
            throw new Exception("Keranjang kosong, tidak ada yang bisa dibeli.");
        }
        
        for (KeranjangItem item : keranjang) {
            // DAO sekarang mengembalikan ID Transaksi
            int idTransaksi = dao.prosesTransaksi(idPelanggan, item); 
            lastIdTransaksi = idTransaksi;
            
            // Hitung total untuk invoice
            double hargaItem = hitungTotalHargaItem(item.getIdJenisProduk(), item.getJumlahKg()); // GUNAKAN GETTER
            totalAkhir += hargaItem;
            
            // Tambahkan detail ke invoice
            invoiceDetail.append("  - ID: ").append(item.getIdJenisProduk()) // GUNAKAN GETTER
                         .append(", Kategori: ").append(item.getKategori())   // GUNAKAN GETTER
                         .append(", Jumlah: ").append(item.getJumlahKg()).append(" kg") // GUNAKAN GETTER
                         .append(", Harga: ").append(Service.KepalaDashboardService.fmtRp(hargaItem))
                         .append("\n");
        }
        
        String invoiceText = "Pembelian Berhasil Diproses pada " + LocalDate.now() + ":\n"
             + "------------------------------------------------------\n"
             + "ID Transaksi Utama: #" + lastIdTransaksi + "\n"
             + invoiceDetail.toString()
             + "------------------------------------------------------\n"
             + "TOTAL BELANJA: " + Service.KepalaDashboardService.fmtRp(totalAkhir);
             
        return new TransaksiResult(invoiceText, lastIdTransaksi);
    }
    
    /**
     * Mengambil dan memformat riwayat transaksi untuk ditampilkan di UI.
     */
    public List<BarisHistory> ambilHistoryPembelian(int idPelanggan) throws SQLException {
        // Panggil DAO
        List<PelangganDAO.TransaksiHistoryRow> rawData = dao.getHistoryPembelian(idPelanggan);

        List<BarisHistory> uiData = new ArrayList<>();
        for (PelangganDAO.TransaksiHistoryRow row : rawData) {
            uiData.add(new BarisHistory(
                row.idTransaksi(),
                row.jenisProduk(),
                row.jumlahProduk(),
                // Format harga dan tanggal
                Service.KepalaDashboardService.fmtRp(row.totalHarga()),
                row.tanggalTransaksi().toString()
            ));
        }
        return uiData;
    }
}
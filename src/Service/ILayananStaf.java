/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import java.sql.SQLException;
import java.util.List;
import Service.LayananStaf.BarisBahanBakuUntukTabel;
import Service.LayananStaf.BarisKonsumsiUntukTabel;
import Service.LayananStaf.BarisProduksiUntukTabel;
import Service.LayananStaf.BarisSisaPanganUntukTabel;
/**
 *
 * @author Zyrus
 */
public interface ILayananStaf {
    // Metode untuk melihat tabel
    List<BarisProduksiUntukTabel> ambilDataProduksiUntukStaf(int idPenggunaStaf) throws SQLException;
    List<BarisKonsumsiUntukTabel> ambilDataKonsumsiUntukStaf(int idPenggunaStaf) throws SQLException;
    List<BarisBahanBakuUntukTabel> ambilDataBahanBaku() throws SQLException;
    List<BarisSisaPanganUntukTabel> ambilDataSisaPanganUntukStaf(int idPenggunaStaf) throws SQLException;

    // Metode untuk mengisi dropdown
    List<String> ambilDaftarKonsumsiTersedia(int idPenggunaStaf) throws SQLException;
    List<String> ambilDaftarBahanBakuTersedia() throws SQLException;

    // Metode untuk operasi C/D/U Sisa Pangan
    void tambahSisaPanganBaru(int idPenggunaStaf, int idSumber, String tipeSumber, 
                              String kategori, int totalKg, String tanggalYmd) throws SQLException;
    boolean hapusSisaPangan(int idPenggunaStaf, int idSisaPangan) throws SQLException;
    boolean updateSisaPangan(int idPenggunaStaf, int idSisaPanganTarget, int totalKgBaru, 
                             String tanggalYmdBaru) throws SQLException;
}
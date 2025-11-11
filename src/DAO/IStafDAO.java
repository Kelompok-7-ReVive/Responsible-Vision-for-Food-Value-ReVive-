/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import java.sql.SQLException;
import java.util.List;
import DAO.StafDAO.BarisBahanBaku;
import DAO.StafDAO.BarisKonsumsi;
import DAO.StafDAO.BarisProduksi;
import DAO.StafDAO.BarisSisaPangan;
import DAO.StafDAO.PilihanID;
/**
 *
 * @author Zyrus
 */
public interface IStafDAO {
// Metode untuk mengambil data pengguna dan wilayah
    String ambilWilayahStaf(int idPenggunaStaf) throws SQLException;
    
    // Metode untuk melihat tabel
    List<BarisProduksi> ambilProduksiBerdasarkanWilayah(String wilayah) throws SQLException;
    List<BarisKonsumsi> ambilKonsumsiBerdasarkanWilayah(String wilayah) throws SQLException;
    List<BarisBahanBaku> ambilSemuaBahanBaku() throws SQLException;
    List<BarisSisaPangan> ambilSisaPanganBerdasarkanWilayah(String wilayah) throws SQLException;

    // Metode untuk operasi C/U/D Sisa Pangan
    List<PilihanID> ambilKonsumsiAktif(String wilayah) throws SQLException;
    List<PilihanID> ambilBahanBakuAktif() throws SQLException;
    
    void tambahSisaPangan(int idPenggunaStaf, int idSumber, String tipeSumber, 
                          String kategori, int totalKg, String tanggalYmd) throws SQLException;
                          
    boolean hapusSisaPangan(int idSisaPangan, String wilayahStaf) throws SQLException;
    
    boolean updateSisaPangan(int idSisaPanganTarget, int totalKgBaru, 
                             String tanggalYmdBaru, String wilayahStaf) throws SQLException;
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class KeranjangItem {
    
    public final int idJenisProduk;
    public final String kategori;    
    public final double jumlahKg;    

    public KeranjangItem(int idJenisProduk, String kategori, double jumlahKg) {
        this.idJenisProduk = idJenisProduk;
        this.kategori = kategori;
        this.jumlahKg = jumlahKg;
    }
    
    // Public Getters (WAJIB digunakan di DAO dan Service)
    public int getIdJenisProduk() { return idJenisProduk; }
    public String getKategori() { return kategori; }
    public double getJumlahKg() { return jumlahKg; }
}
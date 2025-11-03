/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class RingkasanKPI {
    private final double rataRataKg;       // kartu hijau
    private final String wilayahTertinggi; // kartu oranye (nama wilayah / "Tidak ada data")
    private final double wilayahTertinggiKg;
    private final int totalAnalisisWilayah; // 10 (statis)

    public RingkasanKPI(double rataRataKg, String wilayahTertinggi, double wilayahTertinggiKg, int totalAnalisisWilayah) {
        this.rataRataKg = rataRataKg;
        this.wilayahTertinggi = wilayahTertinggi;
        this.wilayahTertinggiKg = wilayahTertinggiKg;
        this.totalAnalisisWilayah = totalAnalisisWilayah;
    }

    public double getRataRataKg() { return rataRataKg; }
    public String getWilayahTertinggi() { return wilayahTertinggi; }
    public double getWilayahTertinggiKg() { return wilayahTertinggiKg; }
    public int getTotalAnalisisWilayah() { return totalAnalisisWilayah; }
}

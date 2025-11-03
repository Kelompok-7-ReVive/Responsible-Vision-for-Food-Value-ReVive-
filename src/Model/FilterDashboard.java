/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class FilterDashboard {
        private final int tahun;
    private final int bulan;     // 1=Jan ... 12=Des
    private final String wilayah; // "Lihat Semua" atau salah satu wilayah

    public FilterDashboard(int tahun, int bulan, String wilayah) {
        this.tahun = tahun;
        this.bulan = bulan;
        this.wilayah = wilayah;
    }

    public int getTahun() { return tahun; }
    public int getBulan() { return bulan; }
    public String getWilayah() { return wilayah; }
}

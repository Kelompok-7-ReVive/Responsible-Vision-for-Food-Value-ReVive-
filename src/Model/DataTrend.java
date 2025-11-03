/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class DataTrend {
    private String labelWaktu;  
    private double totalNilai;
    private String satuan;

    public DataTrend(String labelWaktu, double totalNilai, String satuan) {
        this.labelWaktu = labelWaktu;
        this.totalNilai = totalNilai;
        this.satuan = satuan;
    }

    public String getLabelWaktu() { return labelWaktu; }
    public double getTotalNilai() { return totalNilai; }
    public String getSatuan() { return satuan; }
}

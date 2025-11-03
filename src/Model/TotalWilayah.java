/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class TotalWilayah {
    private final String wilayah;
    private final double totalKg;

    public TotalWilayah (String wilayah, double totalKg) {
        this.wilayah = wilayah;
        this.totalKg = totalKg;
    }

    public String getWilayah() { return wilayah; }
    public double getTotalKg() { return totalKg; }
}
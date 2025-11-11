/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
// import UIPelanggan.BerandaPelanggan; // (Nanti diaktifkan saat Anda membuat beranda pelanggan)
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import UIPelanggan.PelangganBeranda;
/**
 *
 * @author Zyrus
 */
public class Pelanggan extends UserAccount {
    private String mitra;
    private Object UIPelanggan;

    public Pelanggan(int idUser, String nama, String email, String password, String mitra) {
        super(idUser, nama, email, password, "Pelanggan", null, 0);
        this.mitra = mitra;
    }
    
    @Override
    public void bukaBeranda(JFrame jendeleIni) {
        new UIPelanggan.PelangganBeranda(this).setVisible(true);
        jendeleIni.dispose();
    }
    
    public String getMitra() {
        return mitra;
    }

    public void setMitra(String mitra) {
        this.mitra = mitra;
    }
}
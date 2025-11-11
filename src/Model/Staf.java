/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import UIStaffAdministrasi.BerandaStaff;
import javax.swing.JFrame;
/**
 *
 * @author Zyrus
 */
public class Staf extends UserAccount {

    public Staf(int idUser, String nama, String email, String password, String wilayahDikelola, int idHotel) {
        // Memanggil constructor UserAccount (7 argumen)
        super(idUser, nama, email, password, "Staff_administrasi", wilayahDikelola, idHotel);
    }

    /**
     * [PILAR POLYMORPHISM]
     * Implementasi spesifik dari metode abstrak bukaBeranda.
     * Untuk Staf, ini akan membuka jendela BerandaStaff.
     */
    @Override
    public void bukaBeranda(JFrame jendeleIni) {
        // Pastikan nama kelas BerandaStaff Anda sudah benar
        new BerandaStaff(this).setVisible(true); 
        jendeleIni.dispose(); // Menutup jendela login
    }
}
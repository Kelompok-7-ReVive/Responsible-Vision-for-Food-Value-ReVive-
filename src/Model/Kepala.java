/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import UIKepalaAdministrasi.BerandaKepala;
import javax.swing.JFrame;
/**
 *
 * @author Zyrus
 */
public class Kepala extends UserAccount {
    
    public Kepala(int idUser, String nama, String email, String password, String wilayahDikelola) {
        super(idUser, nama, email, password, "Kepala_administrasi", wilayahDikelola, 0);
    }
    
    public Kepala(int idUser, String nama, String email, String password, String wilayahDikelola, int idHotel) {
    super(idUser, nama, email, password, "Kepala_administrasi", wilayahDikelola, idHotel);
}

    @Override
    public void bukaBeranda(JFrame jendeleIni) {
        new BerandaKepala(this).setVisible(true);
        jendeleIni.dispose();
    }
}
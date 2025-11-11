/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;
import javax.swing.JFrame;
/**
 *
 * @author Zyrus
 */
public abstract class UserAccount {

    private int idUser;
    private int idHotel;
    private String nama;
    private String email;
    private String password;
    private String role;
    private String wilayahDikelola;

    /**
     * Constructor dasar untuk diwarisi oleh kelas turunan.
     */
    public UserAccount(int idUser, String nama, String email, String password, String role, String wilayahDikelola, int idHotel) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
        this.wilayahDikelola = wilayahDikelola;
        this.idHotel = idHotel;
    }
    
    /**
     * [BARU] Ini adalah pilar POLIMORFISME dan ABSTRAKSI.
     * Kita memaksa setiap kelas turunan (Kepala, Staf, Pelanggan)
     * untuk mengimplementasikan (override) metode ini dengan cara mereka sendiri.
     * * @param jendeleIni Jendela (JFrame) yang memanggil, biasanya untuk di-dispose().
     */
    
    public abstract void bukaBeranda(JFrame jendeleIni);

    
    // --- Getters and Setters (Encapsulation) ---
    
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public int getIdHotel() { return idHotel; }
    public void setIdHotel(int idHotel) { this.idHotel = idHotel; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getWilayahDikelola() { return wilayahDikelola; }
    public void setWilayahDikelola(String wilayahDikelola) { this.wilayahDikelola = wilayahDikelola; }
}
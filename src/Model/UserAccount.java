/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author Zyrus
 */
public class UserAccount {
    private int idUser;
    private int idHotel;
    private String nama;
    private String email;
    private String password;
    private String role;
    private String wilayahDikelola; // [BARU] Variabel untuk menyimpan wilayah

    /**
     * Constructor kosong, seringkali dibutuhkan oleh library atau framework.
     */
    public UserAccount() {
    }

    /**
     * [BARU] Constructor lengkap untuk membuat objek UserAccount dengan semua data.
     * Ini yang akan memperbaiki error di metode main Anda.
     */
    public UserAccount(int idUser, String nama, String email, String password, String role, String wilayahDikelola) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
        this.wilayahDikelola = wilayahDikelola;
    }
    
    // --- Getters and Setters ---
    
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
    
    // [BARU] Getter dan Setter untuk wilayahDikelola
    public String getWilayahDikelola() { return wilayahDikelola; }
    public void setWilayahDikelola(String wilayahDikelola) { this.wilayahDikelola = wilayahDikelola; }
}
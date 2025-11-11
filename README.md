# Responsible Vision for Food Value (ReVive)

## 📌 Deskripsi Singkat
ReVive merupakan sebuah inisiatif yang berfokus pada pengurangan food waste dan penerapan konsumsi serta produksi yang berkelanjutan. Program ini memanfaatkan sistem digital dan manajemen stok cerdas untuk membantu pelaku usaha, khususnya di sektor makanan, dalam mengelola bahan dan produk dengan lebih efisien serta ramah lingkungan. ReVive juga mengelompokkan sisa makanan menjadi FreshFood (masih layak konsumsi) dan Compost (tidak layak konsumsi) agar dapat dimanfaatkan kembali secara tepat. Dengan demikian, ReVive mendorong terciptanya perilaku environmentally friendly production dan sustainable consumption guna menghidupkan kembali bumi melalui kebiasaan konsumsi dan produksi yang bertanggung jawab.

Flowchart : https://drive.google.com/file/d/19-VoX4V3gOPwsosr-_1vWu2NQQ-NxREc/view?usp=sharing

Use Case Diagram : https://drive.google.com/file/d/1-fnCY6TcUxBld7xoDqOIG4k9d2j5bBqc/view?usp=sharing

## ✨ Fitur Program
Berikut adalah fitur–fitur utama dalam ReVive:
1. _**Sistem Pemantauan**_

Fitur ini digunakan untuk memantau perkembangan food waste. Dashboard menampilkan visualisasi data seperti Bar Chart, ringkasan jumlah food waste, dan Sisa Pangan per Bulan.
Selain itu, tersedia tabel data yang memuat daftar pencatatan food waste secara detail, sehingga pengguna dapat melihat, menelusuri, maupun memfilter data yang sudah tercatat.
Fungsi Utama nya : 
   - Menyajikan grafik Bar Chart food waste berdasarkan periode waktu (Bulan).
   - Memberikan ringkasan jumlah food waste total dan perubahan nilai.
   - Menampilkan tabel data food waste secara lengkap dan terstruktur

2. _**CRUD Data (Create, Read, Update, Delete)**_

Fitur CRUD memungkinkan Kepala Administrasi & Staff Administrasi untuk melakukan pengelolaan data food waste secara fleksibel.
   - Menambahkan data pencatatan food waste baru (Create).
   -  Menampilkan dan melihat data yang sudah tersimpan (Read).
   - Mengubah atau memperbaiki data yang sudah ada (Update).
   - Menghapus data yang tidak valid atau salah input (Delete).

3. _**Fitur Transaksi**_

Fitur Transaksi digunakan untuk Pelanggan dapat melakukan Transaksi dengan kategori Fresh Food (makanan yang belum disentuh sama sekali) dan Kompos (Makanan yang sudah disentuh dan tidak habis atau makanan yang sudah kadaluwarsa).

## 🧠 Penerapan OOP
### 🔒 Encapsulation
Encapsulation adalah proses untuk menyembunyikan data (atribut) dan hanya memperbolehkan akses melalui method khusus (getter dan setter). Tujuannya agar data tidak bisa diubah sembarangan dari luar Class.
```java
package Model;

public class Pelanggan {
    private int id;
    private String nama;
    private String alamat;
    private String noTelp;

    public Pelanggan(int id, String nama, String alamat, String noTelp) {
        this.id = id;
        this.nama = nama;
        this.alamat = alamat;
        this.noTelp = noTelp;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp;
    }
}
```
### 🧬 Inheritance
Inheritance adalah kemampuan Class untuk mewarisi atribut dan metode dari Class lain, sehingga bisa menghindari duplikasi kode.
```java
package Model;

public class Staf extends UserAccount {
    private String divisi;

    public Staf(String username, String password, String role, String divisi) {
        super(username, password, role);
        this.divisi = divisi;
    }

    public String getDivisi() {
        return divisi;
    }

    public void setDivisi(String divisi) {
        this.divisi = divisi;
    }
}
```
### 🧩 Abstraction
Abstraction adalah menyembunyikan detail implementasi dan hanya menampilkan fitur penting atau kontrak perilaku.
```java
package DAO;

import Model.Staf;
import java.util.List;

public interface IStafDAO {
    void tambahStaf(Staf staf);
    void hapusStaf(int id);
    List<Staf> getSemuaStaf();
}
```
### 🎭 Polymorphism
Polymorphism berarti “banyak bentuk” atau satu method yang bisa punya perilaku berbeda tergantung gimana objeknya memanggil.
```java
package Service;

import DAO.IStafDAO;
import Model.Staf;
import java.util.List;

public class LayananStaf implements ILayananStaf {
    private IStafDAO stafDAO;

    public LayananStaf(IStafDAO stafDAO) {
        this.stafDAO = stafDAO;
    }

    @Override
    public void tambahStaf(Staf staf) {
        stafDAO.tambahStaf(staf);
    }

    @Override
    public void hapusStaf(int id) {
        stafDAO.hapusStaf(id);
    }

    @Override
    public List<Staf> getSemuaStaf() {
        return stafDAO.getSemuaStaf();
    }
}
```
### ⚙️ Interface
Interface adalah bentuk abstraksi murni, dimana hanya menyediakan method tanpa implementasi. Class yang “mengimplementasikan” interface wajib menulis isi method-nya sendiri.
```java
package Service;

import Model.Staf;
import java.util.List;

public interface ILayananStaf {
    void tambahStaf(Staf staf);
    void hapusStaf(int id);
    List<Staf> getSemuaStaf();
}
```

## 📂 Struktur Folder / Package
```java
📦 ReVive
┣ 📂 src
┃ ┣ 📂 DAO                     # 
┃ ┣ 📂 Image                   # 
┃ ┣ 📂 Model                   # 
┃ ┣ 📂 Repository              # 
┃ ┣ 📂 Service                 # 
┃ ┣ 📂 UIKepalaAdministrasi    #
┃ ┣ 📂 UIStaffAdministrasi     #
┃ ┣ 📂 UITampilanUtama         # 
┃ ┗ 📂 UIPelanggan             # 
```
### 📂 DAO
```java
📂 DAO
┣ 🧾 AuthDAO.java
┣ 🧾 IStaffDAO.java
┣ 🧾 JenisProduk.java
┣ 🧾 JenisProdukDAO.java
┣ 🧾 JenisProdukDAOImpl.java
┣ 🧾 KepalaDashboardDAO.java
┣ 🧾 StaffDAO.java
┣ 🧾 TransaksiDAO.java
┗ 🧾 TransaksiDAOImpl.java
```
### 📂 Model
```java
📂 Model
┣ 🧾 DataTrend.java
┣ 🧾 FilterDashboard.java
┣ 🧾 Kepala.java
┣ 🧾 KeranjangItem.java
┣ 🧾 RingkasanKPI.java
┣ 🧾 Staf.java
┣ 🧾 TotalWilayah.java
┗ 🧾 UserAccount.java
```
### 📂 Repository
```java
📂 Repository
┣ 🧾 CrudRepository.java
┣ 🧾 JenisProdukRepository.java
┣ 🧾 SisaPanganRepository.java
┣ 🧾 TransaksiRepository.java
┗ 🧾 UserRepository.java
```
### 📂 Service
```java
📂 Service
┣ 🧾 AuthService.java
┣ 🧾 CheckoutService.java
┣ 🧾 DBConnection.java
┣ 🧾 Harga.java
┣ 🧾 ILayananStaf.java
┣ 🧾 KepalaDashboardService.java
┣ 🧾 LayananStaff.java
┣ 🧾 ProdukService.java
┣ 🧾 RegisterService.java
┗ 🧾 TransaksiService.java
```
### 📂 UIKepalaAdministrasi
```java
📂 UIKepalaAdministrasi
┣ 🧾 BerandaKepala.java
┣ 🧾 DashboardKepala.java
┣ 🧾 LihatTransaksi.java
┣ 🧾 PenjualKepala.java
┣ 🧾 TabelKepala.java
┣ 🧾 TambahHapusProduk.java
┗ 🧾 UpdateProduk.java
```
### 📂 UIStaffAdministrasi
```java
📂 UIStaffAdministrasi
┣ 🧾 BerandaStaff.java
┣ 🧾 LihatTabel.java
┣ 🧾 TabelBahanBaku.java
┣ 🧾 TabelKonsumsi.java
┣ 🧾 TabelProduksi.java
┣ 🧾 TambahHapusSisaPangan.java
┗ 🧾 UpdateSisaPangan.java
```
### 📂 UITampilanUtama
```java
📂 UITampilanUtama
┣ 🧾 BerandaUtama.java
┣ 🧾 Login.java
┣ 🧾 Register.java
┣ 🧾 TentangKami.java
┗ 🧾 UtamaDashboard.java
```
### 📂 UIPelanggan??
```java
📂 UIPelanggan?
┣ 🧾 PelangganBelanja.java
┣ 🧾 PelangganBeranda.java
┣ 🧾 PelangganBuktiPembayaran.java
┗ 🧾 PelangganHistoryPembelian.java
```
## 📂 Library/Framework (Nilai Tambah)
- **JDBC (Java Database Connectivity)**

JDBC merupakan library bawaan Java yang berfungsi sebagai jembatan antara aplikasi Java dengan berbagai sistem manajemen basis data seperti MySQL, PostgreSQL, atau SQLite. Melalui JDBC, program dapat mengirimkan perintah SQL seperti SELECT, INSERT, UPDATE, dan DELETE untuk mengelola data yang tersimpan dalam database.

berisi kode seperti :
```java
package Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_NAME = "revive";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Connection conn;

    private DBConnection() {}

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Koneksi ke Database Berhasil!");
            }
        } catch (SQLException e) {
            System.err.println("Koneksi Gagal: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC MySQL tidak ditemukan!");
        }
        return conn;
    }
}
```
- **DAO pattern**

Tujuan utama penggunaan DAO adalah agar kode yang berhubungan dengan interaksi ke database (seperti menjalankan query dan mengelola hasilnya) tidak bercampur dengan kode yang berhubungan dengan aturan atau proses bisnis aplikasi.

berisi kode seperti :
```java
package DAO;
import Service.DBConnection;
import Model.KeranjangItem; 
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PelangganDAO {

    public record BarisProdukToko(
        int idJenisProduk, String kategori, int jumlahKg, double totalHarga,
        String wilayah, Date tanggalKadaluwarsa, Date tanggalDibuat
    ) {}

    public record HargaProduk(
        String kategori, double hargaPerKg, int stokTersedia, String wilayahKepala
    ) {}

    public record TransaksiHistoryRow(
        int idTransaksi, String jenisProduk, int jumlahProduk, double totalHarga, Date tanggalTransaksi
    ) {}

    private static double hargaPerKg(String kategoriLower) {
        return switch (kategoriLower) {
            case "fresh" -> 10_000.0;
            case "kompos" -> 5_000.0;
            default -> 0.0;
        };
    }

    public List<BarisProdukToko> ambilProdukTersedia(String wilayah, String kategori) throws SQLException {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String sql = "SELECT id_jenis_produk, kategori_produk, jumlah_produk, total_harga, wilayah, tanggal_kadaluwarsa, tanggal_dibuat FROM jenis_produk WHERE jumlah_produk > 0";

        if (wilayah != null && !wilayah.isEmpty()) {
            conditions.add("wilayah = ?");
            params.add(wilayah);
        }
        if (kategori != null && !kategori.isEmpty()) {
            conditions.add("kategori_produk = ?");
            params.add(kategori);
        }
```

- **Java Swing**

Java Swing dipakai untuk membuat Aplikasi Desktop. Semua tampilan berupa form, tombol, tabel, input field, dibuat dengan Swing.


## 🖼️ Screenshot GUI
1. **GUI Tampilan Utama**

- Tampilan Beranda Utama
<img width="1028" height="811" alt="image" src="https://github.com/user-attachments/assets/e8762871-dbdb-42d1-85d7-6eed5342eaf7" />


- Tampilan Tentang Kami
<img width="995" height="1023" alt="image" src="https://github.com/user-attachments/assets/538fcab4-2224-4e74-9ace-d74142665915" />
<img width="998" height="334" alt="image" src="https://github.com/user-attachments/assets/0fc163ba-1c2f-45f6-8335-577bcfda4288" />


- Tampilan Dashboard
<img width="1115" height="767" alt="image" src="https://github.com/user-attachments/assets/2c610a59-538e-4f32-85b9-140469264c28" />


- Tampilan Login
<img width="911" height="650" alt="image" src="https://github.com/user-attachments/assets/5f5c65d5-f180-46a5-ac44-e5491b614222" />


- Tampilan Register
<img width="914" height="646" alt="image" src="https://github.com/user-attachments/assets/5c9ef0be-88c3-4005-8fc3-9034df187127" />


2. **GUI Kepala Administrasi**

- Tampilan Beranda Kepala Administrasi
<img width="1357" height="831" alt="image" src="https://github.com/user-attachments/assets/08c29788-837c-44e2-8172-ff4e7e73f048" />


- Tampilan Dashboard Kepala Administrasi
<img width="1583" height="853" alt="image" src="https://github.com/user-attachments/assets/71b5f2f3-a988-45e9-9178-595992f894ee" />

- Tampilan Penjualan Kepala Administrasi
<img width="1003" height="503" alt="image" src="https://github.com/user-attachments/assets/76bc88e7-528f-4371-9cac-ac4ac8565be5" />

- Tampilan Lihat Transaksi Kepala Administrasi
<img width="1213" height="425" alt="image" src="https://github.com/user-attachments/assets/98482a2e-5d41-4038-b74c-27c3e9b56495" />

- Tampilan Tambah/Hapus Produk Kepala Administrasi
<img width="1511" height="659" alt="image" src="https://github.com/user-attachments/assets/cd453ca7-a9ee-4b76-9e9d-9948e52c1c46" />

- Tampilan Update Produk Kepala Administrasi
<img width="1514" height="597" alt="image" src="https://github.com/user-attachments/assets/6a5a3127-4f89-4482-b349-d9013c5ec916" />


3. **GUI Staff Administrasi**

- Tampilan Beranda Staff Administrasi
<img width="1352" height="829" alt="image" src="https://github.com/user-attachments/assets/f3aec01c-be88-4786-bf72-3dd3e04d4980" />

- Tampilan Tabel Staff Administrasi
<img width="1127" height="485" alt="Screenshot 2025-11-04 113521" src="https://github.com/user-attachments/assets/e706d9b5-7373-43b8-bfd5-0cc0876d46bf" />

- Tampilan Tabel Produksi Staff Administrasi
<img width="1397" height="630" alt="image" src="https://github.com/user-attachments/assets/2df97c57-b217-4901-9bd0-874338fff579" />

- Tampilan Tabel Konsumsi Staff Administrasi
<img width="1398" height="637" alt="image" src="https://github.com/user-attachments/assets/bd0efe16-43aa-43ee-a61d-27fb6adadda0" />

- Tampilan Tabel Bahan Baku Staff Administrasi
<img width="1687" height="633" alt="image" src="https://github.com/user-attachments/assets/644960db-0a11-4b2e-a75c-b724fca0ac0d" />

- Tampilan Atur tambah/hapus Tabel Sisa Pangan Staff Administrasi
<img width="1575" height="636" alt="image" src="https://github.com/user-attachments/assets/bef49493-80be-4a05-b35a-920d14fd2528" />

- Tampilan Atur Update Tabel Sisa Pangan Staff Administrasi
<img width="1451" height="634" alt="image" src="https://github.com/user-attachments/assets/24097cc1-29f0-43dc-b7a1-ef8637249e16" />


4. **GUI Pelanggan**

- Tampilan Beranda Pelanggan
<img width="1236" height="626" alt="image" src="https://github.com/user-attachments/assets/a8d184fe-5008-4768-bfa9-572412177066" />

- Tampilan Belanja Pelanggan
<img width="1410" height="636" alt="image" src="https://github.com/user-attachments/assets/4882bb1e-67c0-46f7-b604-861f97bddb73" />

- Tampilan History Pembelian Pelanggan
<img width="1411" height="633" alt="image" src="https://github.com/user-attachments/assets/5504a916-47ae-4152-817a-98332f31406e" />

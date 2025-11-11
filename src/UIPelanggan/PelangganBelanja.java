/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package UIPelanggan;
import Model.Pelanggan; // [BARU] Import kelas Pelanggan
import Model.UserAccount;
import Service.LayananPelanggan; // [BARU] Import LayananPelanggan
import Model.KeranjangItem; // [BARU] Import KeranjangItem
import Model.Staf;
import UIStaffAdministrasi.LihatTabel;
import UITampilanUtama.BerandaUtama;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author CHRISTIAN
 */
public class PelangganBelanja extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PelangganBelanja.class.getName());
    
    // ==== KONTEKS PENGGUNA & SERVICE ====
    private final Pelanggan currentUser; // [DIUBAH] Variabel untuk menyimpan Pelanggan
    private final LayananPelanggan pelangganSrv = new LayananPelanggan(); // [BARU]
    
    // ==== MODEL DATA KERANJANG ====
    private DefaultTableModel modelProduk; // Untuk tabel atas (Jenis Produk)
    private DefaultTableModel modelKeranjang; // Untuk tabel bawah (Keranjang)
    private List<KeranjangItem> keranjang = new ArrayList<>(); // List untuk menyimpan item di keranjang

    // ==== KONSTANTA BATAS BELI ====
    private static final int MIN_KG = 1;
    private static final int MAX_KG = 50;

    /**
     * Creates new form Pelanggan_Toko
     */
    public PelangganBelanja(Pelanggan user) {
        if (user == null) {
             JOptionPane.showMessageDialog(null, "Error: Akun Pelanggan tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
             System.exit(0);
        }
        this.currentUser = user;
        initComponents();
        afterInit();
    }
    
    private void afterInit() {
        setLocationRelativeTo(null);
        setTitle("Belanja Produk - " + currentUser.getNama());
        jLabel2.setText(currentUser.getNama() + " (" + currentUser.getRole() + ")");
        
        setupTabel();
        
        // Hubungkan listener untuk filtering produk
        Wilayah.addActionListener(e -> muatProdukTersedia());
        Kategori.addActionListener(e -> muatProdukTersedia());
        
        // Muat data produk pertama kali
        muatProdukTersedia();
    }

    private void setupTabel() {
        // Tabel ATAS: Jenis Produk Tersedia
        modelProduk = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "WILAYAH", "KATEGORI PRODUK", "JUMLAH (KG)", "HARGA/KG", "TGL KADALUWARSA"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JenisProduk.setModel(modelProduk);
        JenisProduk.getTableHeader().setReorderingAllowed(false);

        // Tabel BAWAH: Keranjang Pelanggan
        modelKeranjang = new DefaultTableModel(
            new Object[][]{},
            new String[]{"ID", "KATEGORI", "JUMLAH (KG)", "TOTAL HARGA"}
        );
        KeranjangPelanggan.setModel(modelKeranjang);
        KeranjangPelanggan.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * Memuat produk tersedia (JenisProduk) berdasarkan filter Wilayah/Kategori.
     */
    private void muatProdukTersedia() {
        try {
            modelProduk.setRowCount(0); // Kosongkan tabel produk
            
            String wilayahFilter = (String) Wilayah.getSelectedItem();
            String kategoriFilter = (String) Kategori.getSelectedItem();
            
            if ("Lihat Semua".equals(wilayahFilter)) wilayahFilter = null;
            if ("Lihat Semua".equals(kategoriFilter)) kategoriFilter = null;

            List<LayananPelanggan.BarisProdukToko> daftarProduk = pelangganSrv.ambilProdukTersedia(
                wilayahFilter, kategoriFilter
            );

            for (LayananPelanggan.BarisProdukToko produk : daftarProduk) {
                modelProduk.addRow(new Object[]{
                    produk.idJenisProduk(),
                    produk.wilayah(),
                    produk.kategori(),
                    produk.jumlahKg(),
                    "Rp " + formatRibuan(produk.hargaPerKg()), 
                    produk.tanggalKadaluwarsa()
                });
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memuat produk", ex);
            JOptionPane.showMessageDialog(this, "Gagal memuat data produk: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Memperbarui tabel keranjang (bawah) dan Total Harga.
     */
    private void perbaruiKeranjangUI() {
        modelKeranjang.setRowCount(0);
        double totalBelanja = 0.0;
        
        for (KeranjangItem item : keranjang) {
            double hargaItem = pelangganSrv.hitungTotalHargaItem(item.idJenisProduk, item.jumlahKg);
            totalBelanja += hargaItem;

            modelKeranjang.addRow(new Object[]{
                item.idJenisProduk,
                item.kategori,
                item.jumlahKg,
                "Rp " + formatRibuan(hargaItem)
            });
        }
        
        TotalHarga.setText("Total: " + "Rp " + formatRibuan(totalBelanja));
    }
    
    // --- Logika Tombol ---
    
    private void prosesTambahKeKeranjang() {
        int selectedRow = JenisProduk.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih produk dari tabel di atas terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Ambil data dari baris terpilih
            int idProduk = (int) modelProduk.getValueAt(selectedRow, 0); // Kolom ID
            String kategori = (String) modelProduk.getValueAt(selectedRow, 2); // Kolom Kategori
            int stokTersedia = (int) modelProduk.getValueAt(selectedRow, 3); // Kolom Jumlah (KG)
            
            // 2. Validasi jumlah input
            String sJumlah = JumlahProduk.getText();
            if (!isAngkaBulatan(sJumlah)) {
                throw new IllegalArgumentException("Jumlah harus berupa angka bulat.");
            }
            int jumlahBeli = Integer.parseInt(sJumlah);

            if (jumlahBeli < MIN_KG || jumlahBeli > MAX_KG) {
                throw new IllegalArgumentException("Jumlah harus antara " + MIN_KG + " kg dan " + MAX_KG + " kg.");
            }
            if (jumlahBeli > stokTersedia) {
                 throw new IllegalArgumentException("Stok tidak mencukupi. Tersedia: " + stokTersedia + " kg.");
            }
            
            // 3. Tambahkan ke keranjang (List lokal)
            KeranjangItem newItem = new KeranjangItem(idProduk, kategori, jumlahBeli);
            keranjang.add(newItem);
            
            // 4. Update UI
            perbaruiKeranjangUI();
            JumlahProduk.setText("");
            
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Input Gagal", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal menambah ke keranjang", ex);
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menambah item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void prosesBeliProduk() {
        if (keranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja Anda kosong.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Panggil Service untuk memproses transaksi dan mendapatkan hasil DTO (TransaksiResult)
            // DTO ini berisi ID Transaksi dan Invoice Text
            LayananPelanggan.TransaksiResult result = pelangganSrv.prosesPembelian(currentUser.getIdUser(), keranjang);

            // 2. Kosongkan keranjang setelah berhasil
            keranjang.clear();

            // 3. Tampilkan Invoice (pop up)
            JOptionPane.showMessageDialog(this, result.invoiceText(), "Invoice Pembelian", JOptionPane.INFORMATION_MESSAGE);

            // 4. Update UI dan Refresh Stok
            perbaruiKeranjangUI();
            muatProdukTersedia(); // Refresh stok produk

            // 5. Arahkan ke halaman upload dengan ID Transaksi
            new PelangganBuktiPembayaran(this.currentUser, result.idTransaksi()).setVisible(true);
            dispose(); // Tutup jendela Belanja

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memproses pembelian", ex);
            JOptionPane.showMessageDialog(this, "Pembelian gagal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // PERBAIKAN: Menambahkan titik koma yang hilang


    private void prosesKosongkanKeranjang() {
        if (keranjang.isEmpty()) return;
        
        int pilihan = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin mengosongkan keranjang?",
            "Konfirmasi", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (pilihan == JOptionPane.YES_OPTION) {
            keranjang.clear();
            perbaruiKeranjangUI();
        }
    }

    // --- Helpers ---
    private boolean isAngkaBulatan(String teks) {
        if (teks == null || teks.isBlank()) return false;
        try {
            return Integer.parseInt(teks) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static String formatRibuan(double nilai) {
        // Helper untuk memformat angka dengan pemisah ribuan (misal: 10.000)
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###.##");
        return formatter.format(nilai).replace(',', '#').replace('.', ',').replace('#', '.');
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        Keluar = new javax.swing.JButton();
        BelanjaPelanggan = new javax.swing.JButton();
        BerandaPelanggan = new javax.swing.JButton();
        HistoryPelanggan = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        JumlahProduk = new javax.swing.JTextField();
        Wilayah = new javax.swing.JComboBox<>();
        Kategori = new javax.swing.JComboBox<>();
        TotalHarga = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        KeranjangPelanggan = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        JenisProduk = new javax.swing.JTable();
        Tambah = new javax.swing.JButton();
        Beli = new javax.swing.JButton();
        Kosongkan = new javax.swing.JButton();
        Keluar1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(207, 217, 224));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(19, 52, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(290, 750));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Keluar.setBackground(new java.awt.Color(19, 52, 30));
        Keluar.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        Keluar.setForeground(new java.awt.Color(255, 255, 255));
        Keluar.setText("KELUAR");
        Keluar.setBorder(null);
        Keluar.setBorderPainted(false);
        Keluar.setContentAreaFilled(false);
        Keluar.setMargin(new java.awt.Insets(7, 14, 3, 14));
        Keluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KeluarActionPerformed(evt);
            }
        });
        jPanel2.add(Keluar, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 350, 140, 40));

        BelanjaPelanggan.setBackground(new java.awt.Color(19, 52, 30));
        BelanjaPelanggan.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        BelanjaPelanggan.setForeground(new java.awt.Color(255, 255, 255));
        BelanjaPelanggan.setText("BELANJA");
        BelanjaPelanggan.setBorder(null);
        BelanjaPelanggan.setBorderPainted(false);
        BelanjaPelanggan.setContentAreaFilled(false);
        BelanjaPelanggan.setMargin(new java.awt.Insets(7, 14, 3, 14));
        BelanjaPelanggan.setSelected(true);
        BelanjaPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BelanjaPelangganActionPerformed(evt);
            }
        });
        jPanel2.add(BelanjaPelanggan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 140, 40));

        BerandaPelanggan.setBackground(new java.awt.Color(19, 52, 30));
        BerandaPelanggan.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        BerandaPelanggan.setForeground(new java.awt.Color(255, 255, 255));
        BerandaPelanggan.setText("BERANDA");
        BerandaPelanggan.setBorder(null);
        BerandaPelanggan.setBorderPainted(false);
        BerandaPelanggan.setContentAreaFilled(false);
        BerandaPelanggan.setMargin(new java.awt.Insets(7, 14, 3, 14));
        BerandaPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BerandaPelangganActionPerformed(evt);
            }
        });
        jPanel2.add(BerandaPelanggan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 140, 40));

        HistoryPelanggan.setBackground(new java.awt.Color(19, 52, 30));
        HistoryPelanggan.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        HistoryPelanggan.setForeground(new java.awt.Color(255, 255, 255));
        HistoryPelanggan.setText("HISTORY BELANJA");
        HistoryPelanggan.setBorder(null);
        HistoryPelanggan.setBorderPainted(false);
        HistoryPelanggan.setContentAreaFilled(false);
        HistoryPelanggan.setMargin(new java.awt.Insets(7, 14, 3, 14));
        HistoryPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HistoryPelangganActionPerformed(evt);
            }
        });
        jPanel2.add(HistoryPelanggan, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 140, 40));

        jPanel4.setBackground(new java.awt.Color(19, 66, 34));

        jLabel1.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("REVIVE");

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Pelanggan");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(32, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        jPanel2.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, 110));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, 510));

        jPanel3.setBackground(new java.awt.Color(204, 81, 0));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        JumlahProduk.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        JumlahProduk.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        JumlahProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JumlahProdukActionPerformed(evt);
            }
        });
        jPanel3.add(JumlahProduk, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, 250, 30));

        Wilayah.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        Wilayah.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lihat Semua", "Balikpapan", "Berau", "Bontang", "Kutai Kartanegara", "Kutai Barat", "Kutai Timur", "Mahakam Ulu", "Penajam Paser Utara", "Samarinda", "Sangatta" }));
        Wilayah.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Wilayah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WilayahActionPerformed(evt);
            }
        });
        jPanel3.add(Wilayah, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 250, -1));

        Kategori.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        Kategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lihat Semua", "Fresh", "Kompos" }));
        Kategori.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        Kategori.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        Kategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KategoriActionPerformed(evt);
            }
        });
        jPanel3.add(Kategori, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 150, 250, -1));

        TotalHarga.setBackground(new java.awt.Color(255, 255, 255));
        TotalHarga.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        TotalHarga.setForeground(new java.awt.Color(255, 255, 255));
        TotalHarga.setText("Total :");
        jPanel3.add(TotalHarga, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 260, 380, -1));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Jumlah Produk (Kg)");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 250, -1));

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Kategori");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 129, 250, 20));

        KeranjangPelanggan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "WILAYAH", "KATEGORI PRODUK", "JUMLAH PRODUK (KG)", "TANGGAL KADALUARSA"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Float.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(KeranjangPelanggan);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 300, 570, 180));

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Wilayah");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 50, -1));

        JenisProduk.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "WILAYAH", "KATEGORI PRODUK", "JUMLAH PRODUK (KG)", "TANGGAL KADALUARSA"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Float.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(JenisProduk);

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 70, 570, 180));

        Tambah.setBackground(new java.awt.Color(235, 156, 53));
        Tambah.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        Tambah.setText("Tambah");
        Tambah.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Tambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TambahActionPerformed(evt);
            }
        });
        jPanel3.add(Tambah, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 310, 120, 40));

        Beli.setBackground(new java.awt.Color(235, 156, 53));
        Beli.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        Beli.setText("Beli");
        Beli.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Beli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BeliActionPerformed(evt);
            }
        });
        jPanel3.add(Beli, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 310, 120, 40));

        Kosongkan.setBackground(new java.awt.Color(235, 156, 53));
        Kosongkan.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        Kosongkan.setText("Kosongkan");
        Kosongkan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Kosongkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KosongkanActionPerformed(evt);
            }
        });
        jPanel3.add(Kosongkan, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 390, 120, 40));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, 950, 510));

        Keluar1.setBackground(new java.awt.Color(78, 113, 68));
        Keluar1.setFont(new java.awt.Font("Myanmar Text", 1, 14)); // NOI18N
        Keluar1.setForeground(new java.awt.Color(255, 255, 255));
        Keluar1.setText("HISTORY BELANJA");
        Keluar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Keluar1.setMargin(new java.awt.Insets(7, 14, 3, 14));
        Keluar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Keluar1ActionPerformed(evt);
            }
        });
        jPanel1.add(Keluar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 280, 140, 40));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void KeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KeluarActionPerformed
        new BerandaUtama().setVisible(true);
        dispose();
    }//GEN-LAST:event_KeluarActionPerformed

    private void BelanjaPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BelanjaPelangganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BelanjaPelangganActionPerformed

    private void BerandaPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BerandaPelangganActionPerformed
        new PelangganBeranda(this.currentUser).setVisible(true);
        dispose();
    }//GEN-LAST:event_BerandaPelangganActionPerformed

    private void TambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TambahActionPerformed
        prosesTambahKeKeranjang();
    }//GEN-LAST:event_TambahActionPerformed

    private void KosongkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KosongkanActionPerformed
        prosesKosongkanKeranjang();
    }//GEN-LAST:event_KosongkanActionPerformed

    private void BeliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BeliActionPerformed
        prosesBeliProduk();
    }//GEN-LAST:event_BeliActionPerformed

    private void Keluar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Keluar1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Keluar1ActionPerformed

    private void HistoryPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HistoryPelangganActionPerformed
        new PelangganHistoryPembelian(this.currentUser).setVisible(true);
        dispose();
    }//GEN-LAST:event_HistoryPelangganActionPerformed

    private void WilayahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WilayahActionPerformed
        muatProdukTersedia();
    }//GEN-LAST:event_WilayahActionPerformed

    private void KategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KategoriActionPerformed
        muatProdukTersedia();
    }//GEN-LAST:event_KategoriActionPerformed

    private void JumlahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JumlahProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JumlahProdukActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }

            // [PERBAIKAN] Gunakan kelas konkret Pelanggan (hanya 5 argumen yang diperlukan)
            Model.Pelanggan penggunaTes = new Model.Pelanggan(
                1, // ID Pelanggan
                "Eko Wijaya", 
                "ekowijayacool@gmail.com", 
                "ekopintar11", 
                "PT Nusantara Globalindo" // Mitra
            );

            // Panggil constructor yang benar dengan user palsu
            java.awt.EventQueue.invokeLater(() -> new PelangganBelanja(penggunaTes).setVisible(true));
        }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BelanjaPelanggan;
    private javax.swing.JButton Beli;
    private javax.swing.JButton BerandaPelanggan;
    private javax.swing.JButton HistoryPelanggan;
    private javax.swing.JTable JenisProduk;
    private javax.swing.JTextField JumlahProduk;
    private javax.swing.JComboBox<String> Kategori;
    private javax.swing.JButton Keluar;
    private javax.swing.JButton Keluar1;
    private javax.swing.JTable KeranjangPelanggan;
    private javax.swing.JButton Kosongkan;
    private javax.swing.JButton Tambah;
    private javax.swing.JLabel TotalHarga;
    private javax.swing.JComboBox<String> Wilayah;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
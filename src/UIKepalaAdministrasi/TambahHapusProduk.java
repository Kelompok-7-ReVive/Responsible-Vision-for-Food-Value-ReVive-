/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package UIKepalaAdministrasi;
import Model.Kepala;
import Model.UserAccount;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import UIKepalaAdministrasi.PenjualanKepala;

/**
 *
 * @author Zyrus
 */
public class TambahHapusProduk extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = 
            java.util.logging.Logger.getLogger(TambahHapusProduk.class.getName());
    
    // ==== KONTEKS LOGIN ====
    private final UserAccount kepalaLogin;

    // ==== LAYANAN (SERVICE) ====
    // Catatan: nama kelas servicemu juga “ProdukService”. Supaya jelas, pakai fully-qualified.
    private final Service.ProdukService produkSrv = new Service.ProdukService();

    // ==== FORMAT & KONSTANTA ====
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MIN_KG = 5;
    private static final int MAX_KG = 100;
    private static final int HARGA_FRESH = 10_000;
    private static final int HARGA_KOMPOS = 5_000;

    // ==== TABEL MODEL ====
    private DefaultTableModel model;

    /**
     * Creates new form KepalaAdministrasi
     */

    /**
     * Konstruktor utama: terima user kepala agar wilayah & hak akses benar.
     */
    public TambahHapusProduk(UserAccount kepalaLogin) {
    // Pastikan kepalaLogin tidak null
        if (kepalaLogin == null) {
            throw new IllegalArgumentException("User kepala tidak boleh null.");
        }
        this.kepalaLogin = kepalaLogin;
        initComponents();
        afterInit();
    }

    // ---- Inisialisasi lanjutan setelah initComponents() ----
    private void afterInit() {
        setLocationRelativeTo(null);
        setTitle("Tambah / Hapus Produk - Kepala Administrasi");

        // Perbaiki kolom JTable: tipe & non-editable
        model = new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "ID JENIS PRODUK", "KATEGORI PRODUK", "JUMLAH (KG)",
                        "TOTAL HARGA", "WILAYAH", "TANGGAL KADALUWARSA", "TANGGAL DIBUAT"
                }
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return switch (col) {
                    case 0 -> Integer.class;     // id
                    case 1 -> String.class;      // kategori
                    case 2 -> Integer.class;     // jumlah kg
                    case 3 -> String.class;      // total harga (tampil Rp 10.000)
                    case 4 -> String.class;      // wilayah
                    case 5 -> String.class;      // tgl kadaluwarsa
                    case 6 -> String.class;      // tgl dibuat
                    default -> Object.class;
                };
            }
        };
        Jenis_Produk.setModel(model);
        Jenis_Produk.getTableHeader().setReorderingAllowed(false);

        // Isi combobox kategori
        kKategoriProduk.removeAllItems();
        kKategoriProduk.addItem("...");     // index 0
        kKategoriProduk.addItem("Fresh");   // index 1
        kKategoriProduk.addItem("Kompos");  // index 2

        // Muat data awal ke tabel
        muatUlangTabel();
    }

    // =========================
    // ==== UTIL & VALIDASI ====
    // =========================

    private void muatUlangTabel() {
        try {
            model.setRowCount(0);

            // Ambil data dari service; service akan memfilter sesuai id kepala (wilayah)
            int idKepala = (kepalaLogin != null ? kepalaLogin.getIdUser() : 0);
            List<Service.ProdukService.BarisProduk> rows = produkSrv.ambilSemuaProdukKepala(idKepala);

            for (Service.ProdukService.BarisProduk r : rows) {
                model.addRow(new Object[]{
                        r.idJenisProduk(),
                        r.kategori(),                  // "fresh"/"kompos" atau "Fresh"/"Kompos" tergantung service
                        r.jumlahKg(),
                        formatRupiah(r.totalHarga()),  // tampil "Rp 10.000"
                        r.wilayah(),
                        nullToStrip(r.tanggalKadaluwarsa()),
                        nullToStrip(r.tanggalDibuat())
                });
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memuat tabel", ex);
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String nullToStrip(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String formatRupiah(double v) {
        // Sesuai preferensi tampilan: "Rp 10.000"
        long bulat = Math.round(v);
        String s = String.format("%,d", bulat).replace(',', '.');
        return "Rp " + s;
    }

    private static boolean isAngkaBulatan(String teks) {
        if (teks == null || teks.isBlank()) return false;
        for (int i = 0; i < teks.length(); i++) {
            if (!Character.isDigit(teks.charAt(i))) return false;
        }
        return true;
    }

    private static LocalDate parseTglAtauNull(String s, String nama) throws IllegalArgumentException {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim(), DF);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(nama + " harus berformat yyyy-MM-dd");
        }
    }

    private static String normalisasiKategori(String uiValue) {
        if (uiValue == null) return "";
        String v = uiValue.trim().toLowerCase();
        if (v.equals("fresh") || v.equals("kompos")) return v;
        return ""; // tidak valid
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField4 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        TambahProduk = new javax.swing.JButton();
        Kembali = new javax.swing.JButton();
        Update = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Jenis_Produk = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        JumlahProduk = new javax.swing.JTextField();
        TanggalDibuat = new javax.swing.JTextField();
        TanggalKadaluwarsa = new javax.swing.JTextField();
        Tambah = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        kKategoriProduk = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        IdProduk = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        Hapus = new javax.swing.JButton();

        jTextField4.setText("jTextField1");
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel4.setBackground(new java.awt.Color(19, 52, 30));

        jPanel1.setBackground(new java.awt.Color(19, 66, 34));

        jLabel1.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("REVIVE");

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Kepala Administrasi");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel1)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        TambahProduk.setBackground(new java.awt.Color(19, 65, 30));
        TambahProduk.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        TambahProduk.setForeground(new java.awt.Color(255, 255, 255));
        TambahProduk.setText("Tambah/Hapus");
        TambahProduk.setBorder(null);
        TambahProduk.setBorderPainted(false);
        TambahProduk.setContentAreaFilled(false);
        TambahProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TambahProdukActionPerformed(evt);
            }
        });

        Kembali.setBackground(new java.awt.Color(19, 65, 30));
        Kembali.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        Kembali.setForeground(new java.awt.Color(255, 255, 255));
        Kembali.setText("Kembali");
        Kembali.setBorder(null);
        Kembali.setBorderPainted(false);
        Kembali.setContentAreaFilled(false);
        Kembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KembaliActionPerformed(evt);
            }
        });

        Update.setBackground(new java.awt.Color(19, 65, 30));
        Update.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        Update.setForeground(new java.awt.Color(255, 255, 255));
        Update.setText("Update");
        Update.setBorder(null);
        Update.setBorderPainted(false);
        Update.setContentAreaFilled(false);
        Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(TambahProduk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(Kembali, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(Update, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64)
                .addComponent(TambahProduk)
                .addGap(18, 18, 18)
                .addComponent(Update)
                .addGap(18, 18, 18)
                .addComponent(Kembali)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        Jenis_Produk.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        Jenis_Produk.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID JENIS PRODUK", "KATEGORI PRODUK", "JUMLAH PRODUK", "TOTAL HARGA", "WILAYAH", "TANGGAL KADALUWARSA", "TANGGAL DIBUAT"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(Jenis_Produk);

        jPanel2.setBackground(new java.awt.Color(19, 52, 30));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(19, 52, 30)));

        JumlahProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JumlahProdukActionPerformed(evt);
            }
        });

        TanggalDibuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalDibuatActionPerformed(evt);
            }
        });

        TanggalKadaluwarsa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalKadaluwarsaActionPerformed(evt);
            }
        });

        Tambah.setBackground(new java.awt.Color(78, 113, 69));
        Tambah.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        Tambah.setForeground(new java.awt.Color(255, 255, 255));
        Tambah.setText("Tambah");
        Tambah.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Tambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TambahActionPerformed(evt);
            }
        });

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(204, 204, 204));
        jLabel3.setText("Kategori Produk");

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 204, 204));
        jLabel4.setText("Tanggal Dibuat");

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(204, 204, 204));
        jLabel7.setText("Tanggal Kadaluwarsa");

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(204, 204, 204));
        jLabel8.setText("Jumlah Produk");

        kKategoriProduk.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        kKategoriProduk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", "Fresh", "Kompos" }));
        kKategoriProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kKategoriProdukActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(181, 81, 0));

        IdProduk.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("ID PRODUK");

        jLabel6.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("*masukkan id yang akan dihapus");

        Hapus.setBackground(new java.awt.Color(204, 0, 0));
        Hapus.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        Hapus.setForeground(new java.awt.Color(255, 255, 255));
        Hapus.setText("Hapus");
        Hapus.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Hapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HapusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(52, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(IdProduk)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6))
                    .addComponent(Hapus, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(IdProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(Hapus)
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3)
                    .addComponent(TanggalKadaluwarsa)
                    .addComponent(jLabel7)
                    .addComponent(kKategoriProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TanggalDibuat, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JumlahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel4))
                .addGap(42, 42, 42)
                .addComponent(Tambah, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, 0)
                        .addComponent(kKategoriProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, 0)
                        .addComponent(JumlahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TanggalKadaluwarsa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TanggalDibuat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                        .addComponent(Tambah)
                        .addGap(79, 79, 79))))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void JumlahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JumlahProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JumlahProdukActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void TanggalDibuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalDibuatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalDibuatActionPerformed

    private void TanggalKadaluwarsaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalKadaluwarsaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalKadaluwarsaActionPerformed

    private void TambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TambahActionPerformed
        // === Validasi & kirim ke service ===
        try {
            String kategori = normalisasiKategori((String) kKategoriProduk.getSelectedItem());
            if (kategori.isEmpty()) {
                throw new IllegalArgumentException("Silakan pilih kategori (Fresh/Kompos).");
            }

            String sJumlah = JumlahProduk.getText();
            if (!isAngkaBulatan(sJumlah)) {
                throw new IllegalArgumentException("Jumlah produk harus bilangan bulat.");
            }
            int jumlah = Integer.parseInt(sJumlah);
            if (jumlah < MIN_KG || jumlah > MAX_KG) {
                throw new IllegalArgumentException("Jumlah harus antara " + MIN_KG + "–" + MAX_KG + " kg.");
            }

            String sTglDibuat = TanggalDibuat.getText();
            String sTglKadalu = TanggalKadaluwarsa.getText();

            // Eksklusif: tak boleh isi dua-duanya
            if (!sTglDibuat.isBlank() && !sTglKadalu.isBlank()) {
                throw new IllegalArgumentException("Tanggal Dibuat dan Tanggal Kadaluwarsa tidak boleh diisi bersamaan.");
            }

            LocalDate tDibuat = parseTglAtauNull(sTglDibuat, "Tanggal Dibuat");
            LocalDate tKadal = parseTglAtauNull(sTglKadalu, "Tanggal Kadaluwarsa");

            // Wajib sesuai kategori
            if (kategori.equals("fresh")) {
                if (tKadal == null) throw new IllegalArgumentException("Kategori Fresh: isi Tanggal Kadaluwarsa (yyyy-MM-dd).");
                if (tDibuat != null) throw new IllegalArgumentException("Kategori Fresh: Tanggal Dibuat harus kosong.");
            } else { // kompos
                if (tDibuat == null) throw new IllegalArgumentException("Kategori Kompos: isi Tanggal Dibuat (yyyy-MM-dd).");
                if (tKadal != null) throw new IllegalArgumentException("Kategori Kompos: Tanggal Kadaluwarsa harus kosong.");
            }

            int idKepala = (kepalaLogin != null ? kepalaLogin.getIdUser() : 0);

            // Kirim ke service
            produkSrv.tambahProdukBaru(
                    idKepala,
                    kategori,          // "fresh"/"kompos"
                    jumlah,            // int
                    (tKadal == null ? null : tKadal.format(DF)),
                    (tDibuat == null ? null : tDibuat.format(DF)),
                    0.0
            );

            // Berhasil → refresh tabel & kosongkan input
            JOptionPane.showMessageDialog(this, "Produk berhasil ditambahkan.");
            kosongkanInput();
            muatUlangTabel();

        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal menambah produk", ex);
            JOptionPane.showMessageDialog(this, "Gagal menambah produk: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_TambahActionPerformed

    private void kKategoriProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kKategoriProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_kKategoriProdukActionPerformed

    private void TambahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TambahProdukActionPerformed

    }//GEN-LAST:event_TambahProdukActionPerformed

    private void KembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KembaliActionPerformed
        // Buka kembali halaman PenjualanKepala sambil mengirim data user yang login
        new PenjualanKepala((Kepala) this.kepalaLogin).setVisible(true);

        // Tutup halaman saat ini
        dispose();
    }//GEN-LAST:event_KembaliActionPerformed

    private void UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateActionPerformed
        new UpdateProduk(this.kepalaLogin).setVisible(true);
        dispose();
    }//GEN-LAST:event_UpdateActionPerformed

    private void HapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HapusActionPerformed
        try {
            String sId = IdProduk.getText();
            if (!isAngkaBulatan(sId)) {
                throw new IllegalArgumentException("ID Produk harus bilangan bulat.");
            }
            int id = Integer.parseInt(sId);
            int idKepala = (kepalaLogin != null ? kepalaLogin.getIdUser() : 0);

            Service.ProdukService.HasilHapus hasil = produkSrv.hapusProdukJikaMemenuhiSyarat(idKepala, id);
            switch (hasil) {
                case BERHASIL -> {
                    JOptionPane.showMessageDialog(this, "Produk berhasil dihapus.");
                    IdProduk.setText("");
                    muatUlangTabel();
                }
                case SUDAH_ADA_TRANSAKSI -> {
                    JOptionPane.showMessageDialog(this, "Tidak dapat menghapus: sudah ada transaksi.");
                }
                case TIDAK_DITEMUKAN -> {
                    JOptionPane.showMessageDialog(this, "ID tidak ditemukan.");
                }
                case TIDAK_BERWENANG -> {
                    JOptionPane.showMessageDialog(this, "Anda tidak berwenang menghapus produk ini.");
                }
            }
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal menghapus produk", ex);
            JOptionPane.showMessageDialog(this, "Gagal menghapus produk: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_HapusActionPerformed
    
    private void kosongkanInput() {
        kKategoriProduk.setSelectedIndex(0);
        JumlahProduk.setText("");
        TanggalDibuat.setText("");
        TanggalKadaluwarsa.setText("");
    }
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

        // [DIUBAH] Buat user palsu untuk tujuan testing, agar constructor terpenuhi.
        // Ganti '1' dan 'Samarinda' sesuai kebutuhan testing Anda.
           Model.Kepala testUser = new Model.Kepala(
               3, // ID User
               "Narendra Augusta",
               "test@mail.com",
               "pass",
               "Kutai Kartanegara", // Wilayah
               0 // ID HOTEL (Argumen ini sekarang tidak diperlukan karena kita hapus dari constructor Kepala)
            );

        // Panggil constructor yang benar dengan user palsu
        java.awt.EventQueue.invokeLater(() -> new TambahHapusProduk(testUser).setVisible(true));
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Hapus;
    private javax.swing.JTextField IdProduk;
    private javax.swing.JTable Jenis_Produk;
    private javax.swing.JTextField JumlahProduk;
    private javax.swing.JButton Kembali;
    private javax.swing.JButton Tambah;
    private javax.swing.JButton TambahProduk;
    private javax.swing.JTextField TanggalDibuat;
    private javax.swing.JTextField TanggalKadaluwarsa;
    private javax.swing.JButton Update;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JComboBox<String> kKategoriProduk;
    // End of variables declaration//GEN-END:variables
}
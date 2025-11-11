/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package UIKepalaAdministrasi;
import UIKepalaAdministrasi.TambahHapusProduk;
import Model.UserAccount;
import Service.ProdukService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Zyrus
 */
public class UpdateProduk extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UpdateProduk.class.getName());
    
    // ==== KONTEKS LOGIN & SERVICE ====
    private final UserAccount currentUser;
    private final ProdukService produkSrv = new ProdukService();

    // ==== FORMAT & KONSTANTA (Sama seperti TambahHapus) ====
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MIN_KG = 5;
    private static final int MAX_KG = 100;

    // ==== TABEL MODEL ====
    private DefaultTableModel model;

    /**
     * Creates new form UpdateProduk
     */
    public UpdateProduk(UserAccount user) {
        if (user == null) {
            throw new IllegalArgumentException("User Account tidak boleh null.");
        }
        this.currentUser = user;
        initComponents();
        afterInit();
    }
    
    private void afterInit() {
        setLocationRelativeTo(null);
        setTitle("Update Produk - " + currentUser.getNama());

        // Setup JTable (sama seperti di TambahHapusProduk)
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
                    case 0, 2 -> Integer.class;
                    case 3, 4, 5, 6, 1 -> String.class;
                    default -> Object.class;
                };
            }
        };
        jTable1.setModel(model); // Pastikan nama variabel JTable Anda adalah jTable1
        jTable1.getTableHeader().setReorderingAllowed(false);

        // Isi combobox kategori
        kKategoriProduk.removeAllItems();
        kKategoriProduk.addItem("...");
        kKategoriProduk.addItem("Fresh");
        kKategoriProduk.addItem("Kompos");

        // Muat data awal ke tabel
        muatUlangTabel();
        
    }
    
    private void muatUlangTabel() {
        try {
            model.setRowCount(0);
            List<ProdukService.BarisProduk> rows = produkSrv.ambilSemuaProdukKepala(currentUser.getIdUser());

            for (ProdukService.BarisProduk r : rows) {
                model.addRow(new Object[]{
                    r.idJenisProduk(),
                    r.kategori(),
                    r.jumlahKg(),
                    formatRupiah(r.totalHarga()),
                    r.wilayah(),
                    nullToStrip(r.tanggalKadaluwarsa()),
                    nullToStrip(r.tanggalDibuat())
                });
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memuat tabel", ex);
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performUpdate() {
        try {
            // 1. Validasi ID
            String sId = IDJenisProduk.getText();
            if (!isAngkaBulatan(sId)) {
                throw new IllegalArgumentException("ID Produk harus diisi dan berupa angka.");
            }
            int idProduk = Integer.parseInt(sId);

            // 2. Validasi sisa input (sama seperti Tambah)
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

            if (!sTglDibuat.isBlank() && !sTglKadalu.isBlank()) {
                throw new IllegalArgumentException("Hanya salah satu tanggal yang boleh diisi.");
            }
            
            LocalDate tDibuat = parseTglAtauNull(sTglDibuat, "Tanggal Dibuat");
            LocalDate tKadal = parseTglAtauNull(sTglKadalu, "Tanggal Kadaluwarsa");

            if (kategori.equals("fresh") && tKadal == null) {
                throw new IllegalArgumentException("Kategori Fresh wajib mengisi Tanggal Kadaluwarsa.");
            }
            if (kategori.equals("kompos") && tDibuat == null) {
                throw new IllegalArgumentException("Kategori Kompos wajib mengisi Tanggal Dibuat.");
            }

            // 3. Panggil Service untuk melakukan update
            // (Kita akan buat metode ini di Service & DAO selanjutnya)
            boolean berhasil = produkSrv.updateProduk(
                currentUser.getIdUser(), 
                idProduk, 
                kategori, 
                jumlah,
                (tKadal == null ? null : tKadal.format(DF)),
                (tDibuat == null ? null : tDibuat.format(DF))
            );

            // 4. Beri feedback ke user
            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Produk dengan ID " + idProduk + " berhasil diupdate.");
                kosongkanInput();
                muatUlangTabel();
            } else {
                JOptionPane.showMessageDialog(this, "Produk dengan ID " + idProduk + " tidak ditemukan di wilayah Anda.", "Gagal", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal mengupdate produk", ex);
            JOptionPane.showMessageDialog(this, "Gagal mengupdate produk: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void kosongkanInput() {
        IDJenisProduk.setText("");
        kKategoriProduk.setSelectedIndex(0);
        JumlahProduk.setText("");
        TanggalDibuat.setText("");
        TanggalKadaluwarsa.setText("");
    }
    
    private static String nullToStrip(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String formatRupiah(double v) {
        long bulat = Math.round(v);
        return "Rp " + String.format("%,d", bulat).replace(',', '.');
    }

    private static boolean isAngkaBulatan(String teks) {
        if (teks == null || teks.isBlank()) return false;
        for (char c : teks.toCharArray()) {
            if (!Character.isDigit(c)) return false;
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
        return (v.equals("fresh") || v.equals("kompos")) ? v : "";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        TambahProduk = new javax.swing.JButton();
        UpdateButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        kKategoriProduk = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        TanggalKadaluwarsa = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        JumlahProduk = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        TanggalDibuat = new javax.swing.JTextField();
        Update = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        IDJenisProduk = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

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
        TambahProduk.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
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

        UpdateButton.setBackground(new java.awt.Color(19, 65, 30));
        UpdateButton.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        UpdateButton.setForeground(new java.awt.Color(255, 255, 255));
        UpdateButton.setText("Update");
        UpdateButton.setBorder(null);
        UpdateButton.setBorderPainted(false);
        UpdateButton.setContentAreaFilled(false);
        UpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(TambahProduk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(UpdateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(TambahProduk)
                .addGap(18, 18, 18)
                .addComponent(UpdateButton)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(19, 52, 30));

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(204, 204, 204));
        jLabel3.setText("Kategori Produk");

        kKategoriProduk.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        kKategoriProduk.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", "Fresh", "Kompos" }));
        kKategoriProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kKategoriProdukActionPerformed(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(204, 204, 204));
        jLabel7.setText("Tanggal Kadaluwarsa");

        TanggalKadaluwarsa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalKadaluwarsaActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(204, 204, 204));
        jLabel8.setText("Jumlah Produk");

        JumlahProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JumlahProdukActionPerformed(evt);
            }
        });

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 204, 204));
        jLabel4.setText("Tanggal Dibuat");

        TanggalDibuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalDibuatActionPerformed(evt);
            }
        });

        Update.setBackground(new java.awt.Color(78, 113, 69));
        Update.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        Update.setForeground(new java.awt.Color(255, 255, 255));
        Update.setText("Update");
        Update.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateActionPerformed(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Poppins SemiBold", 0, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(204, 204, 204));
        jLabel9.setText("ID Jenis Produk");

        IDJenisProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IDJenisProdukActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(164, 164, 164)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TanggalKadaluwarsa, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(IDJenisProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TanggalDibuat, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JumlahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel4))
                .addGap(42, 42, 42)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Update, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3)
                        .addComponent(kKategoriProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(183, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, 0)
                        .addComponent(kKategoriProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, 0)
                        .addComponent(JumlahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(0, 0, 0)
                        .addComponent(IDJenisProduk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TanggalKadaluwarsa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TanggalDibuat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Update))))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TambahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TambahProdukActionPerformed
        new TambahHapusProduk(this.currentUser).setVisible(true);
        // Tutup jendela Penjualan saat ini
        dispose();
    }//GEN-LAST:event_TambahProdukActionPerformed

    private void UpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UpdateButtonActionPerformed

    private void kKategoriProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kKategoriProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_kKategoriProdukActionPerformed

    private void TanggalKadaluwarsaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalKadaluwarsaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalKadaluwarsaActionPerformed

    private void JumlahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JumlahProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JumlahProdukActionPerformed

    private void TanggalDibuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalDibuatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalDibuatActionPerformed

    private void UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateActionPerformed
        performUpdate();
    }//GEN-LAST:event_UpdateActionPerformed

    private void IDJenisProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IDJenisProdukActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IDJenisProdukActionPerformed

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
            } catch (Exception ex) {
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }

            // Buat user palsu untuk tujuan testing
           Model.Kepala testUser = new Model.Kepala(
               3, // ID User
               "Narendra Augusta",
               "test@mail.com",
               "pass",
               "Kutai Kartanegara", // Wilayah
               0 // ID HOTEL (Argumen ini sekarang tidak diperlukan karena kita hapus dari constructor Kepala)
            );

            java.awt.EventQueue.invokeLater(() -> new UpdateProduk(testUser).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField IDJenisProduk;
    private javax.swing.JTextField JumlahProduk;
    private javax.swing.JButton TambahProduk;
    private javax.swing.JTextField TanggalDibuat;
    private javax.swing.JTextField TanggalKadaluwarsa;
    private javax.swing.JButton Update;
    private javax.swing.JButton UpdateButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JComboBox<String> kKategoriProduk;
    // End of variables declaration//GEN-END:variables
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package UIStaffAdministrasi;
import Model.UserAccount;
import Service.LayananStaf;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author CHRISTIAN
 */
public class UpdateSisaPangan extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(UpdateSisaPangan.class.getName());
    
// ==== KONTEKS PENGGUNA & SERVICE ====
    private final UserAccount penggunaSaatIni;
    private final LayananStaf layananStaf = new LayananStaf();

    // ==== MODEL TABEL & FORMATTER ====
    private DefaultTableModel modelTabel;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Creates new form Staff_Table
     */
    
    public UpdateSisaPangan(UserAccount pengguna) {
        if (pengguna == null) {
            throw new IllegalArgumentException("Akun Pengguna tidak boleh kosong (null).");
        }
        this.penggunaSaatIni = pengguna;
        initComponents();
        setelahInisialisasi();
    }
    
    private void setelahInisialisasi() {
        setTitle("Update Sisa Pangan - " + penggunaSaatIni.getNama());
        jLabel2.setText(penggunaSaatIni.getNama() + " (Staf Administrasi)"); 

        // Atur JTable
        modelTabel = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "ID Sisa Pangan", "ID Konsumsi", "ID Bahan Baku", "Kategori", 
                "Total (Kg)", "Tanggal", "Nama Hotel"
            }
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        TabelSisaPangan.setModel(modelTabel);
        TabelSisaPangan.getTableHeader().setReorderingAllowed(false);
        
        // Muat data untuk pertama kali
        muatUlangTabel();
        
        // Atur Ukuran Jendela & Posisi
        setSize(1180, 560); // Sesuaikan ukuran ini jika perlu
        setLocationRelativeTo(null);
    }
    
    /**
     * Mengambil data dari service dan mengisi tabel.
     */
    private void muatUlangTabel() {
        try {
            modelTabel.setRowCount(0);
            List<LayananStaf.BarisSisaPanganUntukTabel> daftarBaris = layananStaf.ambilDataSisaPanganUntukStaf(penggunaSaatIni.getIdUser());

            for (LayananStaf.BarisSisaPanganUntukTabel baris : daftarBaris) {
                modelTabel.addRow(new Object[]{
                    baris.idSisaPangan(),
                    baris.idKonsumsi() == 0 ? "-" : baris.idKonsumsi(),
                    baris.idBahanBaku() == 0 ? "-" : baris.idBahanBaku(),
                    baris.kategori(),
                    baris.totalSisaPangan(),
                    baris.tanggal(),
                    baris.namaHotel()
                });
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memuat data sisa pangan", ex);
            JOptionPane.showMessageDialog(this, "Gagal memuat data sisa pangan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Mengosongkan semua field input setelah aksi berhasil.
     */
    private void kosongkanInputUpdate() {
        IDSisaPangan.setText(""); // Nama variabel dari initComponents
        TotalSisaPangan.setText("");
        TanggalSisaPangan.setText("");
    }
    
    /**
     * [DIUBAH] Validasi dan logika untuk tombol UPDATE.
     */
    private void prosesUpdateData() {
        try {
            // 1. Validasi ID Sisa Pangan (Target)
            String sIdTarget = IDSisaPangan.getText(); // Menggunakan nama variabel dari initComponents
            if (!isAngkaBulatan(sIdTarget)) {
                throw new IllegalArgumentException("ID Sisa Pangan yang akan di-update harus diisi dan berupa angka.");
            }
            int idSisaPanganTarget = Integer.parseInt(sIdTarget);

            // 2. Validasi Total (Kg)
            String sTotal = TotalSisaPangan.getText();
            if (!isAngkaBulatan(sTotal)) {
                throw new IllegalArgumentException("Total Sisa Pangan baru harus berupa angka (kg).");
            }
            int totalKgBaru = Integer.parseInt(sTotal);
            if (totalKgBaru <= 0) {
                throw new IllegalArgumentException("Total Sisa Pangan baru harus lebih dari 0 kg.");
            }
            
            // 3. Validasi Tanggal
            LocalDate tanggalBaru = parseTglAtauGagal(TanggalSisaPangan.getText(), "Tanggal Sisa Pangan baru");

            // 4. Panggil Service
            // (Kita akan buat metode ini di LayananStaf dan StafDAO)
            boolean berhasil = layananStaf.updateSisaPangan(
                penggunaSaatIni.getIdUser(),
                idSisaPanganTarget,
                totalKgBaru,
                tanggalBaru.format(DF)
            );
            
            // 5. Berhasil
            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Data sisa pangan (ID: " + idSisaPanganTarget + ") berhasil diupdate.");
                kosongkanInputUpdate();
                muatUlangTabel();
            } else {
                JOptionPane.showMessageDialog(this, "Update gagal. Pastikan ID Sisa Pangan ada di wilayah Anda.", "Gagal", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal mengupdate data sisa pangan", ex);
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helper validasi ---
    private static boolean isAngkaBulatan(String teks) {
        if (teks == null || teks.isBlank()) return false;
        for (char c : teks.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private static LocalDate parseTglAtauGagal(String s, String namaField) throws IllegalArgumentException {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(namaField + " tidak boleh kosong.");
        }
        try {
            return LocalDate.parse(s.trim(), DF);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(namaField + " harus berformat yyyy-MM-dd (contoh: 2025-11-03)");
        }
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
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        Update = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        TotalSisaPangan = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        TanggalSisaPangan = new javax.swing.JTextField();
        IDSisaPangan = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        TabelSisaPangan = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ButtonTambahHapus = new javax.swing.JButton();
        ButtonUpdate = new javax.swing.JButton();
        ButtonKembali = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(19, 52, 30));

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("ID Sisa Pangan");

        Update.setBackground(new java.awt.Color(78, 113, 68));
        Update.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        Update.setForeground(new java.awt.Color(255, 255, 255));
        Update.setText("Update");
        Update.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateActionPerformed(evt);
            }
        });

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Total Sisa Pangan (Kg)");

        TotalSisaPangan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        TotalSisaPangan.setMinimumSize(new java.awt.Dimension(64, 25));
        TotalSisaPangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TotalSisaPanganActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Tanggal Sisa Pangan");

        TanggalSisaPangan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        TanggalSisaPangan.setMinimumSize(new java.awt.Dimension(64, 25));
        TanggalSisaPangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalSisaPanganActionPerformed(evt);
            }
        });

        IDSisaPangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IDSisaPanganActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(199, 199, 199)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TotalSisaPangan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IDSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(TanggalSisaPangan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Update, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(265, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(IDSisaPangan)
                    .addComponent(TanggalSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TotalSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Update, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
        );

        jPanel3.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 1050, 180));

        TabelSisaPangan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID SISA PANGAN", "ID KONSUMSI", "ID BAHAN BAKU", "ID USER", "KATEGORI SISA PANGAN", "TOTAL SISA PANGAN (KG)", "TANGGAL SISA PANGAN"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(TabelSisaPangan);

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 1050, 320));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 0, -1, 510));

        jPanel4.setBackground(new java.awt.Color(19, 52, 30));

        jPanel5.setBackground(new java.awt.Color(19, 66, 34));

        jLabel1.setFont(new java.awt.Font("Montserrat", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("REVIVE");

        jLabel2.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Kepala Administrasi");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(31, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(40, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        ButtonTambahHapus.setBackground(new java.awt.Color(19, 65, 30));
        ButtonTambahHapus.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        ButtonTambahHapus.setForeground(new java.awt.Color(255, 255, 255));
        ButtonTambahHapus.setText("Tambah/Hapus");
        ButtonTambahHapus.setBorder(null);
        ButtonTambahHapus.setBorderPainted(false);
        ButtonTambahHapus.setContentAreaFilled(false);
        ButtonTambahHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonTambahHapusActionPerformed(evt);
            }
        });

        ButtonUpdate.setBackground(new java.awt.Color(19, 65, 30));
        ButtonUpdate.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
        ButtonUpdate.setForeground(new java.awt.Color(255, 255, 255));
        ButtonUpdate.setText("Update");
        ButtonUpdate.setBorder(null);
        ButtonUpdate.setBorderPainted(false);
        ButtonUpdate.setContentAreaFilled(false);
        ButtonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonUpdateActionPerformed(evt);
            }
        });

        ButtonKembali.setBackground(new java.awt.Color(19, 65, 30));
        ButtonKembali.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        ButtonKembali.setForeground(new java.awt.Color(255, 255, 255));
        ButtonKembali.setText("Kembali");
        ButtonKembali.setBorder(null);
        ButtonKembali.setBorderPainted(false);
        ButtonKembali.setContentAreaFilled(false);
        ButtonKembali.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonKembaliActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ButtonKembali, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ButtonUpdate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ButtonTambahHapus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(98, 98, 98)
                .addComponent(ButtonTambahHapus)
                .addGap(18, 18, 18)
                .addComponent(ButtonUpdate)
                .addGap(18, 18, 18)
                .addComponent(ButtonKembali)
                .addGap(0, 192, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, 510));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1253, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ButtonTambahHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonTambahHapusActionPerformed
        new TambahHapusSisaPangan(this.penggunaSaatIni).setVisible(true);
        dispose();
    }//GEN-LAST:event_ButtonTambahHapusActionPerformed

    private void ButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonUpdateActionPerformed
        muatUlangTabel();
    }//GEN-LAST:event_ButtonUpdateActionPerformed

    private void ButtonKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonKembaliActionPerformed
        new BerandaStaff(this.penggunaSaatIni).setVisible(true);
        dispose();
    }//GEN-LAST:event_ButtonKembaliActionPerformed

    private void TotalSisaPanganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalSisaPanganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TotalSisaPanganActionPerformed

    private void TanggalSisaPanganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalSisaPanganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalSisaPanganActionPerformed

    private void UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateActionPerformed
        prosesUpdateData();
    }//GEN-LAST:event_UpdateActionPerformed

    private void IDSisaPanganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IDSisaPanganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IDSisaPanganActionPerformed

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
            java.util.logging.Logger.getLogger(LihatTabel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // [DIUBAH] Gunakan kelas konkret Staf dan tambahkan idHotel=0
        Model.Staf penggunaTes = new Model.Staf(
            2, 
            "Mahesa Adi", 
            "test@mail.com", 
            "pass", 
            "Balikpapan", // Wilayah
            10 // idHotel (sesuaikan dengan data Staff Anda)
        );
        
        java.awt.EventQueue.invokeLater(() -> new LihatTabel(penggunaTes).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ButtonKembali;
    private javax.swing.JButton ButtonTambahHapus;
    private javax.swing.JButton ButtonUpdate;
    private javax.swing.JTextField IDSisaPangan;
    private javax.swing.JTable TabelSisaPangan;
    private javax.swing.JTextField TanggalSisaPangan;
    private javax.swing.JTextField TotalSisaPangan;
    private javax.swing.JButton Update;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package UIStaffAdministrasi;
import Model.UserAccount;
import Service.LayananStaf; // Kita akan tambahkan metode baru ke service ini
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author CHRISTIAN
 */
public class TambahHapusSisaPangan extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TambahHapusSisaPangan.class.getName());
    
    // ==== KONTEKS PENGGUNA & SERVICE ====
    private final UserAccount penggunaSaatIni;
    private final LayananStaf layananStaf = new LayananStaf();

    // ==== MODEL TABEL & FORMATTER ====
    private DefaultTableModel modelTabel;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Creates new form Staff_Table
     */
    public TambahHapusSisaPangan(UserAccount pengguna) {
        if (pengguna == null) {
            throw new IllegalArgumentException("Akun Pengguna tidak boleh kosong (null).");
        }
        this.penggunaSaatIni = pengguna;
        initComponents();
        setelahInisialisasi();
    }
    
    private void setelahInisialisasi() {
        setTitle("Tambah/Hapus Sisa Pangan - " + penggunaSaatIni.getNama());
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
        
        // Atur ComboBox dinamis
        DropdownTipeSumber.addActionListener(e -> perbaruiDropdownIDSumber());
        DropdownIDSumber.setEnabled(false);

        // Muat data untuk pertama kali
        muatUlangTabel();
        setLocationRelativeTo(null);
    }
    
    /**
     * [LOGIKA BARU] Mengisi dropdown kedua secara dinamis berdasarkan pilihan di dropdown pertama.
     */
    private void perbaruiDropdownIDSumber() {
        String tipeDipilih = (String) DropdownTipeSumber.getSelectedItem();
        DefaultComboBoxModel<String> modelID = new DefaultComboBoxModel<>();
        DropdownIDSumber.setModel(modelID);

        try {
            if (tipeDipilih.equals("Konsumsi")) {
                // Panggil service untuk daftar ID Konsumsi (Metode ini akan kita buat)
                List<String> daftarKonsumsi = layananStaf.ambilDaftarKonsumsiTersedia(penggunaSaatIni.getIdUser());
                modelID.addElement("...Pilih ID Konsumsi...");
                for (String item : daftarKonsumsi) {
                    modelID.addElement(item);
                }
                DropdownIDSumber.setEnabled(true);
                
            } else if (tipeDipilih.equals("Bahan Baku")) {
                // Panggil service untuk daftar ID Bahan Baku (Metode ini akan kita buat)
                List<String> daftarBahanBaku = layananStaf.ambilDaftarBahanBakuTersedia();
                modelID.addElement("...Pilih ID Bahan Baku...");
                for (String item : daftarBahanBaku) {
                    modelID.addElement(item);
                }
                DropdownIDSumber.setEnabled(true);
                
            } else {
                modelID.addElement("...Pilih Tipe Sumber Dulu...");
                DropdownIDSumber.setEnabled(false);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal memuat daftar ID sumber", ex);
            JOptionPane.showMessageDialog(this, "Gagal memuat daftar ID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Mengambil data dari service dan mengisi tabel.
     */
    private void muatUlangTabel() {
        try {
            modelTabel.setRowCount(0); // Kosongkan tabel
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
    private void kosongkanInputTambah() {
        DropdownTipeSumber.setSelectedIndex(0);
        DropdownIDSumber.setModel(new DefaultComboBoxModel<>(new String[]{"...Pilih Tipe Sumber Dulu..."}));
        DropdownIDSumber.setEnabled(false);
        TotalSisaPangan.setText("");
        TanggalSisaPangan.setText("");
    }
    
    /**
     * [DIUBAH] Validasi dan logika untuk tombol TAMBAH.
     */
    private void prosesTambahData() {
        try {
            // 1. Validasi Tipe (Konsumsi/Bahan Baku)
            String tipeSumber = (String) DropdownTipeSumber.getSelectedItem();
            if (tipeSumber == null || tipeSumber.equals("...")) {
                throw new IllegalArgumentException("Silakan pilih Tipe Sumber (Konsumsi/Bahan Baku).");
            }
            
            // 2. Validasi ID Sumber
            String sumberDipilih = (String) DropdownIDSumber.getSelectedItem();
            if (sumberDipilih == null || sumberDipilih.startsWith("...")) {
                throw new IllegalArgumentException("Silakan pilih ID Sumber yang spesifik.");
            }

            // 3. Ambil ID dan Kategori dari string (misal: "ID 101 (Lauk)")
            int idSumber = Integer.parseInt(sumberDipilih.split(" ")[1]);
            String kategori = sumberDipilih.split("\\(")[1].replace(")", "");

            // 4. Validasi Total (Kg)
            String sTotal = TotalSisaPangan.getText();
            if (!isAngkaBulatan(sTotal)) {
                throw new IllegalArgumentException("Total Sisa Pangan harus berupa angka (kg).");
            }
            int totalKg = Integer.parseInt(sTotal);
            if (totalKg <= 0) {
                throw new IllegalArgumentException("Total Sisa Pangan harus lebih dari 0 kg.");
            }
            
            // 5. Validasi Tanggal
            LocalDate tanggal = parseTglAtauGagal(TanggalSisaPangan.getText(), "Tanggal Sisa Pangan");

            // 6. Panggil Service
            layananStaf.tambahSisaPanganBaru(
                penggunaSaatIni.getIdUser(),
                idSumber,
                tipeSumber, // "Konsumsi" atau "Bahan Baku"
                kategori,
                totalKg,
                tanggal.format(DF) // format "yyyy-MM-dd"
            );
            
            // 7. Berhasil
            JOptionPane.showMessageDialog(this, "Data sisa pangan berhasil ditambahkan.");
            kosongkanInputTambah();
            muatUlangTabel();

        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal menambah data sisa pangan", ex);
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Validasi dan logika untuk tombol HAPUS.
     */
    private void prosesHapusData() {
        try {
            String sId = IdSisaPanganHapus.getText();
            if (!isAngkaBulatan(sId)) {
                throw new IllegalArgumentException("ID Sisa Pangan harus diisi dan berupa angka.");
            }
            int idSisaPangan = Integer.parseInt(sId);
            
            int pilihan = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus data dengan ID " + idSisaPangan + "?",
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (pilihan != JOptionPane.YES_OPTION) {
                return;
            }

            // Panggil Service dengan ID Staf untuk validasi
            boolean berhasil = layananStaf.hapusSisaPangan(
                penggunaSaatIni.getIdUser(), 
                idSisaPangan
            );

            if (berhasil) {
                JOptionPane.showMessageDialog(this, "Data sisa pangan berhasil dihapus.");
                IdSisaPanganHapus.setText("");
                muatUlangTabel();
            } else {
                // Ini terjadi jika ID tidak ada ATAU ID user tidak cocok
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan atau Anda tidak berwenang menghapus data ini.", "Gagal", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Gagal menghapus data", ex);
            JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        jScrollPane2 = new javax.swing.JScrollPane();
        TabelSisaPangan = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        DropdownIDSumber = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        TotalSisaPangan = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        ButtonTambahProduk = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        TanggalSisaPangan = new javax.swing.JTextField();
        DropdownTipeSumber = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        IdSisaPanganHapus = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        ButtonHapus = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ButtonTambahHapus = new javax.swing.JButton();
        ButtonUpdate = new javax.swing.JButton();
        ButtonKembali = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

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

        jPanel6.setBackground(new java.awt.Color(19, 52, 30));

        DropdownIDSumber.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        DropdownIDSumber.setBorder(null);
        DropdownIDSumber.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        DropdownIDSumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DropdownIDSumberActionPerformed(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(255, 255, 255));
        jLabel7.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("ID Sumber/Asal");

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Pilih yang Ingin Ditambahkan");

        TotalSisaPangan.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        TotalSisaPangan.setBorder(null);
        TotalSisaPangan.setMinimumSize(new java.awt.Dimension(64, 25));
        TotalSisaPangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TotalSisaPanganActionPerformed(evt);
            }
        });

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Total Sisa Pangan (Kg)");

        ButtonTambahProduk.setBackground(new java.awt.Color(78, 113, 68));
        ButtonTambahProduk.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        ButtonTambahProduk.setForeground(new java.awt.Color(255, 255, 255));
        ButtonTambahProduk.setText("Tambah");
        ButtonTambahProduk.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ButtonTambahProduk.setBorderPainted(false);
        ButtonTambahProduk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonTambahProdukActionPerformed(evt);
            }
        });

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Tanggal Sisa Pangan");

        TanggalSisaPangan.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        TanggalSisaPangan.setBorder(null);
        TanggalSisaPangan.setMinimumSize(new java.awt.Dimension(64, 25));
        TanggalSisaPangan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TanggalSisaPanganActionPerformed(evt);
            }
        });

        DropdownTipeSumber.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        DropdownTipeSumber.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", "Konsumsi", "Bahan Baku" }));
        DropdownTipeSumber.setBorder(null);
        DropdownTipeSumber.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        DropdownTipeSumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DropdownTipeSumberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(DropdownIDSumber, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))
                        .addGap(14, 14, 14)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(DropdownTipeSumber, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(106, 106, 106))
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(TotalSisaPangan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TanggalSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2)))))
                        .addGap(18, 18, 18)
                        .addComponent(ButtonTambahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(ButtonTambahProduk, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(1, 1, 1)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DropdownIDSumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(DropdownTipeSumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(0, 0, 0)
                        .addComponent(TotalSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, 0)
                        .addComponent(TanggalSisaPangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(35, 35, 35))
        );

        jPanel7.setBackground(new java.awt.Color(196, 80, 27));

        IdSisaPanganHapus.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        IdSisaPanganHapus.setBorder(null);
        IdSisaPanganHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IdSisaPanganHapusActionPerformed(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("ID Sisa Pangan");

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Poppins", 0, 10)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("*masukkan id yang akan dihapus");

        ButtonHapus.setBackground(new java.awt.Color(204, 0, 51));
        ButtonHapus.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        ButtonHapus.setForeground(new java.awt.Color(255, 255, 255));
        ButtonHapus.setText("Hapus");
        ButtonHapus.setBorder(null);
        ButtonHapus.setBorderPainted(false);
        ButtonHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ButtonHapusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(90, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(IdSisaPanganHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(ButtonHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(78, 78, 78))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel9)
                .addGap(0, 0, 0)
                .addComponent(jLabel10)
                .addGap(0, 0, 0)
                .addComponent(IdSisaPanganHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(ButtonHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

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
        ButtonTambahHapus.setFont(new java.awt.Font("Poppins", 1, 14)); // NOI18N
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
        ButtonUpdate.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

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

    private void ButtonTambahHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonTambahHapusActionPerformed
        muatUlangTabel();
    }//GEN-LAST:event_ButtonTambahHapusActionPerformed

    private void ButtonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonUpdateActionPerformed
        new UpdateSisaPangan(this.penggunaSaatIni).setVisible(true);
        dispose();
    }//GEN-LAST:event_ButtonUpdateActionPerformed

    private void ButtonKembaliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonKembaliActionPerformed
        new BerandaStaff(this.penggunaSaatIni).setVisible(true);
        dispose();
    }//GEN-LAST:event_ButtonKembaliActionPerformed

    private void ButtonTambahProdukActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonTambahProdukActionPerformed
        prosesTambahData();
    }//GEN-LAST:event_ButtonTambahProdukActionPerformed

    private void DropdownIDSumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DropdownIDSumberActionPerformed
        perbaruiDropdownIDSumber();
    }//GEN-LAST:event_DropdownIDSumberActionPerformed

    private void DropdownTipeSumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DropdownTipeSumberActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_DropdownTipeSumberActionPerformed

    private void TotalSisaPanganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TotalSisaPanganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TotalSisaPanganActionPerformed

    private void TanggalSisaPanganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TanggalSisaPanganActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TanggalSisaPanganActionPerformed

    private void ButtonHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonHapusActionPerformed
        prosesHapusData();
    }//GEN-LAST:event_ButtonHapusActionPerformed

    private void IdSisaPanganHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IdSisaPanganHapusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_IdSisaPanganHapusActionPerformed

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
    private javax.swing.JButton ButtonHapus;
    private javax.swing.JButton ButtonKembali;
    private javax.swing.JButton ButtonTambahHapus;
    private javax.swing.JButton ButtonTambahProduk;
    private javax.swing.JButton ButtonUpdate;
    private javax.swing.JComboBox<String> DropdownIDSumber;
    private javax.swing.JComboBox<String> DropdownTipeSumber;
    private javax.swing.JTextField IdSisaPanganHapus;
    private javax.swing.JTable TabelSisaPangan;
    private javax.swing.JTextField TanggalSisaPangan;
    private javax.swing.JTextField TotalSisaPangan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
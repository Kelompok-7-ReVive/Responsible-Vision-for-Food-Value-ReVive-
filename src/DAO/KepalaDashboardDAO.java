/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;
import Model.DataTrend;
import Service.DBConnection;
import Model.TotalWilayah;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Zyrus
 */
public class KepalaDashboardDAO {
private static final java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(KepalaDashboardDAO.class.getName());
    
    private Connection getConn() throws SQLException {
        return DBConnection.getConnection();
    }
    
    // =================================================================================
    // DTO BARU UNTUK DATA MULTI-SERIES
    // =================================================================================
    /**
     * DTO sederhana untuk menampung hasil kueri tren multi-wilayah.
     * Mengandung nama wilayah (seriesName) untuk setiap titik data.
     */
    public record SeriesDataPoint(String seriesName, int idx, String label, double total) {}


    // =================================================================================
    // METODE UNTUK SISA PANGAN (Sudah Benar)
    // =================================================================================

    public List<TotalWilayah> getTotalsPerWilayah(int tahun, int bulan) throws SQLException {
        return getSisaPanganWilayah(tahun, bulan, null);
    }

    public List<TotalWilayah> getTop5Wilayah(int tahun, int bulan) throws SQLException {
        return getSisaPanganWilayah(tahun, bulan, 5);
    }

    private List<TotalWilayah> getSisaPanganWilayah(int tahun, int bulan, Integer limit) throws SQLException {
        String baseSql = """
            SELECT COALESCE(TRIM(h.wilayah),'(Tidak diketahui)') AS wilayah,
                   COALESCE(SUM(sp.total_sisa_pangan), 0) AS total_kg
            FROM sisa_pangan sp
            JOIN konsumsi k ON sp.id_konsumsi = k.id_konsumsi
            JOIN produksi p ON k.id_produksi = p.id_produksi
            JOIN hotel h ON p.id_hotel = h.id_hotel
            WHERE YEAR(sp.tanggal_sisa_pangan) = ?
              AND (? = 0 OR MONTH(sp.tanggal_sisa_pangan) = ?)
            GROUP BY COALESCE(TRIM(h.wilayah),'(Tidak diketahui)')
            ORDER BY total_kg DESC
            """;
        
        String finalSql = (limit != null && limit > 0) ? baseSql + " LIMIT ?" : baseSql;

        try (var conn = getConn();
             var ps = conn.prepareStatement(finalSql)) {
            
            int paramIndex = 1;
            ps.setInt(paramIndex++, tahun);
            ps.setInt(paramIndex++, bulan);
            ps.setInt(paramIndex++, bulan);

            if (limit != null && limit > 0) {
                ps.setInt(paramIndex, limit);
            }

            try (var rs = ps.executeQuery()) {
                var list = new ArrayList<TotalWilayah>();
                while (rs.next()) {
                    list.add(new TotalWilayah(
                        rs.getString("wilayah"),
                        rs.getDouble("total_kg")
                    ));
                }
                return list;
            }
        }
    }
    
    // =================================================================================
    // METODE UNTUK DATA TREND (Single dan Multi-Wilayah)
    // =================================================================================

    /**
     * [METODE BARU & LENGKAP] Mengambil data tren untuk SEMUA wilayah sekaligus dalam satu query.
     * Tidak berlaku untuk 'bahan_baku' karena tidak memiliki data wilayah.
     */
    public List<SeriesDataPoint> getTrendForAllWilayah(String tabel, int tahun, int bulan) throws SQLException {
        final boolean perBulan = (bulan == 0);
        String fromJoin, dateCol, sumCol, seriesCol;

        switch (tabel.toLowerCase()) {
            case "produksi" -> {
                fromJoin = "FROM produksi p JOIN hotel h ON p.id_hotel = h.id_hotel";
                dateCol = "p.tanggal_produksi";
                sumCol = "p.total_produksi";
                seriesCol = "h.wilayah";
            }
            case "konsumsi" -> {
                fromJoin = """
                    FROM konsumsi k
                    JOIN produksi p ON k.id_produksi = p.id_produksi
                    JOIN hotel h ON p.id_hotel = h.id_hotel
                    """;
                dateCol = "p.tanggal_produksi";
                sumCol = "k.total_termakan";
                seriesCol = "h.wilayah";
            }
            case "sisa pangan", "sisa_pangan" -> {
                fromJoin = """
                    FROM sisa_pangan sp
                    JOIN konsumsi k ON sp.id_konsumsi = k.id_konsumsi
                    JOIN produksi p ON k.id_produksi = p.id_produksi
                    JOIN hotel h ON p.id_hotel = h.id_hotel
                    """;
                dateCol = "sp.tanggal_sisa_pangan";
                sumCol = "sp.total_sisa_pangan";
                seriesCol = "h.wilayah";
            }
            case "transaksi" -> {
                fromJoin = """
                    FROM transaksi t
                    JOIN kepala_administrasi ka ON t.id_user_kepala = ka.id_user
                    """;
                dateCol = "t.tanggal_transaksi";
                sumCol = "t.total_harga";
                seriesCol = "ka.wilayah_dikelola";
            }
            default -> throw new IllegalArgumentException(
                    "Tabel tidak mendukung query multi-wilayah: " + tabel);
        }

        String selectTime = perBulan
            ? " MONTH(" + dateCol + ") AS idx, DATE_FORMAT(" + dateCol + ", '%b') AS label "
            : " DAY(" + dateCol + ") AS idx, LPAD(DAY(" + dateCol + "), 2, '0') AS label ";
        
        String where = " WHERE YEAR(" + dateCol + ") = ? ";
        if (!perBulan) where += " AND MONTH(" + dateCol + ") = ? ";

        String sql = "SELECT " + selectTime + ", COALESCE(SUM(" + sumCol + "),0) AS total, " + seriesCol + " AS series_name " +
                     fromJoin + where + " GROUP BY series_name, idx, label ORDER BY series_name, idx";
        
        log.fine("SQL getTrendForAllWilayah: " + sql);
        
        List<SeriesDataPoint> results = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int p = 1;
            ps.setInt(p++, tahun);
            if (!perBulan) ps.setInt(p++, bulan);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SeriesDataPoint(
                        rs.getString("series_name"),
                        rs.getInt("idx"),
                        rs.getString("label"),
                        rs.getDouble("total")
                    ));
                }
            }
        }
        return results;
    }

    /**
     * [METODE LAMA, TETAP DIPERLUKAN] Mengambil data tren untuk satu wilayah spesifik,
     * atau untuk 'bahan_baku' yang tidak memiliki data wilayah.
     */
    public List<DataTrend> getTrend(String tabel, int tahun, int bulan, String wilayah) throws SQLException {
        // ... (Implementasi metode getTrend Anda yang lama tidak perlu diubah) ...
        // ... (Silakan salin-tempel kode asli Anda di sini) ...
        final boolean perBulan = (bulan == 0);
        String fromJoin, dateCol, sumCol;

        switch (tabel.toLowerCase()) {
            case "produksi" -> {
                fromJoin = """
                     FROM produksi p
                     JOIN hotel h ON p.id_hotel = h.id_hotel
                     """;
                dateCol = "p.tanggal_produksi";
                sumCol = "p.total_produksi";
            }
            case "konsumsi" -> {
                fromJoin = """
                     FROM konsumsi k
                     JOIN produksi p ON k.id_produksi = p.id_produksi
                     JOIN hotel h ON p.id_hotel = h.id_hotel
                     """;
                dateCol = "p.tanggal_produksi"; // konsumsi tidak punya tanggal sendiri
                sumCol = "k.total_termakan";
            }
            case "bahan baku", "bahan_baku" -> {
                fromJoin = "FROM bahan_baku bb";
                dateCol = "bb.tanggal_kadaluwarsa";
                sumCol = "bb.jumlah_bahan_baku";
            }
            case "sisa pangan", "sisa_pangan" -> {
                fromJoin = """
                     FROM sisa_pangan sp
                     JOIN konsumsi k ON sp.id_konsumsi = k.id_konsumsi
                     JOIN produksi p ON k.id_produksi = p.id_produksi
                     JOIN hotel h ON p.id_hotel = h.id_hotel
                     """;
                dateCol = "sp.tanggal_sisa_pangan";
                sumCol = "sp.total_sisa_pangan";
            }
            case "transaksi" -> {
                fromJoin = """
                     FROM transaksi t
                     JOIN kepala_administrasi ka ON t.id_user_kepala = ka.id_user
                     """;
                dateCol = "t.tanggal_transaksi";
                sumCol = "t.total_harga";
            }
            default -> throw new IllegalArgumentException("Tabel tidak dikenali: " + tabel);
        }

        String selectTime = perBulan
            ? " MONTH(" + dateCol + ") AS idx, DATE_FORMAT(" + dateCol + ", '%b') AS label "
            : " DAY(" + dateCol + ") AS idx, LPAD(DAY(" + dateCol + "), 2, '0') AS label ";

        String where = " WHERE YEAR(" + dateCol + ") = ? ";
        if (!perBulan) where += " AND MONTH(" + dateCol + ") = ? ";

        if (!tabel.equalsIgnoreCase("bahan_baku")) {
            where += " AND (? IS NULL OR ";
            if (tabel.equalsIgnoreCase("transaksi"))
                where += "ka.wilayah_dikelola = ?)";
            else
                where += "h.wilayah = ?)";
        } else {
            // bahan baku tidak punya wilayah
            where += " AND ? IS NULL AND ? IS NULL";
        }

        String sql = "SELECT " + selectTime + ", COALESCE(SUM(" + sumCol + "),0) AS total " +
                       fromJoin + where + " GROUP BY idx, label ORDER BY idx";

        log.fine("SQL getTrend: " + sql);

        Map<Integer, DataTrend> byIndex = new LinkedHashMap<>();

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int p = 1;
            ps.setInt(p++, tahun);
            if (!perBulan) ps.setInt(p++, bulan);

            if (wilayah == null || wilayah.isBlank()) {
                ps.setNull(p++, Types.VARCHAR);
                ps.setNull(p++, Types.VARCHAR);
            } else {
                ps.setString(p++, wilayah);
                ps.setString(p++, wilayah);
            }

            log.fine(() -> String.format("getTrend params => tabel=%s, perBulan=%s, tahun=%d, bulan=%d, wilayah=%s",
                tabel, perBulan, tahun, bulan, (wilayah == null ? "NULL" : wilayah)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idx = rs.getInt("idx");
                    String label = rs.getString("label");
                    double total = rs.getDouble("total");
                    byIndex.put(idx, new DataTrend(label, total, ""));
                }
            }
        }

        // isi nol untuk bulan/hari yang kosong
        List<DataTrend> out = new ArrayList<>();
        if (perBulan) {
            for (int m = 1; m <= 12; m++) {
                DataTrend dt = byIndex.get(m);
                String label = (dt != null) ? dt.getLabelWaktu() :
                    switch (m) {
                        case 1 -> "Jan"; case 2 -> "Feb"; case 3 -> "Mar";
                        case 4 -> "Apr"; case 5 -> "Mei"; case 6 -> "Jun";
                        case 7 -> "Jul"; case 8 -> "Agu"; case 9 -> "Sep";
                        case 10 -> "Okt"; case 11 -> "Nov"; default -> "Des";
                    };
                out.add(dt != null ? dt : new DataTrend(label, 0.0, ""));
            }
        } else {
            int days = java.time.YearMonth.of(tahun, bulan).lengthOfMonth();
            for (int d = 1; d <= days; d++) {
                DataTrend dt = byIndex.get(d);
                String label = (dt != null) ? dt.getLabelWaktu() : String.format("%02d", d);
                out.add(dt != null ? dt : new DataTrend(label, 0.0, ""));
            }
        }

        return out;
    }
}    
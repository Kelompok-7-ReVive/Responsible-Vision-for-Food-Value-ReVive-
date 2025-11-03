/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.KepalaDashboardDAO;
import DAO.KepalaDashboardDAO.SeriesDataPoint;
import Model.FilterDashboard;
import Model.RingkasanKPI;
import Model.TotalWilayah;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import Model.DataTrend;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 *
 * @author Zyrus
 */
public class KepalaDashboardService {
private static final java.util.logging.Logger logger =
        java.util.logging.Logger.getLogger(KepalaDashboardService.class.getName());

    private final KepalaDashboardDAO dao = new KepalaDashboardDAO();

    private static final List<String> LIST_WILAYAH = List.of(
        "Samarinda","Balikpapan","Kutai Kartanegara","Bontang","Sangatta",
        "Berau","Kutai Barat","Kutai Timur","Penajam Paser Utara","Mahakam Ulu"
    );

    private static boolean isSemua(String w) {
        return w == null || w.trim().isEmpty() || "Lihat Semua".equalsIgnoreCase(w.trim());
    }

    /** KPI cards */
    public RingkasanKPI loadKpi(FilterDashboard f) throws SQLException {
        final int tahun  = f.getTahun();
        final int bulan  = f.getBulan(); // 0 = semua bulan
        final String wilayah = f.getWilayah();

        List<TotalWilayah> totals = dao.getTotalsPerWilayah(tahun, bulan);
        if (totals == null) totals = java.util.Collections.emptyList();

        java.util.Map<String, Double> totalsMap = new java.util.HashMap<>();
        for (TotalWilayah tw : totals) {
            String key = (tw.getWilayah() == null ? "" : tw.getWilayah().trim().toLowerCase());
            totalsMap.put(key, tw.getTotalKg());
        }

        double rataKg;
        if (isSemua(wilayah)) {
            double sum = 0;
            for (String w : LIST_WILAYAH) {
                sum += totalsMap.getOrDefault(w.trim().toLowerCase(), 0.0);
            }
            rataKg = LIST_WILAYAH.isEmpty() ? 0 : sum / LIST_WILAYAH.size();
        } else {
            rataKg = totalsMap.getOrDefault(wilayah.trim().toLowerCase(), 0.0);
        }

        Optional<TotalWilayah> top = dao.getTop5Wilayah(tahun, bulan).stream().findFirst();
        String topName = top.map(TotalWilayah::getWilayah).orElse("Tidak ada data");
        double topKg   = top.map(TotalWilayah::getTotalKg).orElse(0.0);

        return new RingkasanKPI(rataKg, topName, topKg, LIST_WILAYAH.size());
    }

    /** Data untuk Bar Chart Top-5 (lintas wilayah; hormati bulan) */
    public List<TotalWilayah> loadTop5(FilterDashboard f) throws SQLException {
        return dao.getTop5Wilayah(f.getTahun(), f.getBulan());
    }

    /** [DIUBAH] Load data untuk SINGLE trend line, kini lebih ringkas */
    public List<DataTrend> loadTrend(String tabel, int tahun, int bulan, String wilayah) throws SQLException {
        String t = normalizeTabelName(tabel);
        
        if ("bahan_baku".equals(t)) {
            wilayah = null;
        }

        String w = isSemua(wilayah) ? null : wilayah.trim();

        List<DataTrend> rows = dao.getTrend(t, tahun, bulan, w);
        
        // Gunakan helper untuk mengisi unit, menghindari duplikasi
        return fillUnit(rows, unitFor(t));
    }
    
    public List<DataTrend> loadTrend(FilterDashboard f, String tabel) throws SQLException {
        return loadTrend(tabel, f.getTahun(), f.getBulan(), f.getWilayah());
    }

    // [DIUBAH] Metode ini sekarang menjadi inti dari logika multi-series
    public List<SeriesTrend> loadTrendSeries(FilterDashboard f, String tabel) throws SQLException {
        String t = normalizeTabelName(tabel);
        String unit = unitFor(t);
        List<SeriesTrend> out = new ArrayList<>();
        boolean semuaWilayah = isSemua(f.getWilayah());

        // KASUS 1: Bahan baku, selalu dianggap "Semua Wilayah" dan tidak dipisah
        if ("bahan_baku".equals(t)) {
            List<DataTrend> pts = dao.getTrend(t, f.getTahun(), f.getBulan(), null);
            out.add(new SeriesTrend("Bahan Baku", unit, fillUnit(pts, unit)));
            return out;
        }
        
        // KASUS 2: Pengguna memilih "Lihat Semua" untuk tabel selain bahan baku
        // Ini adalah bagian yang paling dioptimalkan!
        if (semuaWilayah) {
            // Cukup 1x panggilan ke database untuk semua wilayah
            List<SeriesDataPoint> flatData = dao.getTrendForAllWilayah(t, f.getTahun(), f.getBulan());

            // Kelompokkan data mentah berdasarkan nama wilayah
            Map<String, Map<Integer, DataTrend>> seriesData = new LinkedHashMap<>();
            for (SeriesDataPoint point : flatData) {
                seriesData.computeIfAbsent(point.seriesName(), k -> new LinkedHashMap<>())
                          .put(point.idx(), new DataTrend(point.label(), point.total(), unit));
            }
            
            // Loop melalui daftar wilayah resmi untuk memastikan semua series ada
            // (termasuk yang datanya kosong) dan urutannya konsisten.
            for (String w : LIST_WILAYAH) {
                Map<Integer, DataTrend> pointsMap = seriesData.getOrDefault(w, Collections.emptyMap());
                List<DataTrend> finalPoints = fillMissingDataPoints(pointsMap, f.getTahun(), f.getBulan());
                out.add(new SeriesTrend(w, unit, finalPoints));
            }

        // KASUS 3: Pengguna memilih satu wilayah spesifik
        } else {
            String wilayahTrim = f.getWilayah().trim();
            List<DataTrend> pts = dao.getTrend(t, f.getTahun(), f.getBulan(), wilayahTrim);
            out.add(new SeriesTrend(wilayahTrim, unit, fillUnit(pts, unit)));
        }

        return out;
    }

    // =================================================================================
    // HELPER METHODS (PRIVATE)
    // =================================================================================

    private String normalizeTabelName(String tabel) {
        if (tabel == null) return "";
        return tabel.trim().toLowerCase().replace(' ', '_');
    }

    private static String unitFor(String tabelLower) {
        return switch (tabelLower) {
            case "transaksi" -> "Rp";
            case "produksi", "konsumsi", "bahan_baku", "sisa_pangan" -> "kg";
            default -> "";
        };
    }
    
    // [BARU] Helper untuk mengisi unit ke dalam list DataTrend
    private List<DataTrend> fillUnit(List<DataTrend> data, String unit) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataTrend> out = new ArrayList<>(data.size());
        for (DataTrend dt : data) {
            out.add(new DataTrend(dt.getLabelWaktu(), dt.getTotalNilai(), unit));
        }
        return out;
    }

    // [BARU] Logika pengisian data nol (zero-filling) dipindahkan ke sini
    // agar bisa dipakai oleh data multi-series.
    private List<DataTrend> fillMissingDataPoints(Map<Integer, DataTrend> byIndex, int tahun, int bulan) {
        final boolean perBulan = (bulan == 0);
        List<DataTrend> out = new ArrayList<>();
        
        if (perBulan) {
            for (int m = 1; m <= 12; m++) {
                DataTrend dt = byIndex.get(m);
                String label = (dt != null) ? dt.getLabelWaktu() : 
                    switch (m) {
                        case 1->"Jan"; case 2->"Feb"; case 3->"Mar";
                        case 4->"Apr"; case 5->"Mei"; case 6->"Jun";
                        case 7->"Jul"; case 8->"Agu"; case 9->"Sep";
                        case 10->"Okt"; case 11->"Nov"; default->"Des";
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
    
    // =================================================================================
    // HELPERS & DTO (PUBLIC)
    // =================================================================================
    
    public static String namaBulan(int bulan1To12) {
        String[] id = {"Januari","Februari","Maret","April","Mei","Juni",
                         "Juli","Agustus","September","Oktober","November","Desember"};
        if (bulan1To12 < 1 || bulan1To12 > 12) return "-";
        return id[bulan1To12 - 1];
    }
    
    public static String fmtRp(double rupiah) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return nf.format(rupiah);
    }

    public static String fmtKg(double kg) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        nf.setMaximumFractionDigits(2);
        return nf.format(kg) + " kg";
    }
    
    public static class SeriesTrend {
        private final String name;
        private final String unit;
        private final List<DataTrend> points;

        public SeriesTrend(String name, String unit, List<DataTrend> points) {
            this.name = name;
            this.unit = unit;
            this.points = points == null ? Collections.emptyList() : points;
        }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public List<DataTrend> getPoints() { return points; }
    }
}
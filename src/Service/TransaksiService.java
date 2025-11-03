/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.TransaksiDAO;
import DAO.TransaksiDAO.TransaksiRow;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Zyrus
 */
public class TransaksiService {
    private final TransaksiDAO dao = new TransaksiDAO();

    public record BarisTransaksi(
        int idTransaksi,
        String namaPelanggan,
        int idJenisProduk,
        String jenisProduk,
        String tanggalTransaksi, // Diubah menjadi String untuk kemudahan display
        int jumlahProduk,
        double totalHarga
    ) {}

    public List<BarisTransaksi> getTransaksiForKepala(int idUserKepala) throws SQLException {
        // 1. Panggil DAO untuk mendapatkan data mentah dari database
        List<TransaksiRow> rawData = dao.getTransaksiForKepala(idUserKepala);
        
        // 2. Transformasi data mentah menjadi format yang siap ditampilkan (DTO)
        List<BarisTransaksi> uiData = new ArrayList<>();
        for (TransaksiRow row : rawData) {
            uiData.add(new BarisTransaksi(
                row.idTransaksi(),
                row.namaPelanggan(),
                row.idJenisProduk(),
                row.jenisProduk(),
                row.tanggalTransaksi().toString(), // Ubah java.sql.Date menjadi String "yyyy-MM-dd"
                row.jumlahProduk(),
                row.totalHarga()
            ));
        }
        
        return uiData;
    }    
}

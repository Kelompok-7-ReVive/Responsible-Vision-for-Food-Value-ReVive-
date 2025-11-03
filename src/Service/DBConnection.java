/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Zyrus
 */
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
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Repository;
import Service.DBConnection;
import Model.UserAccount;
import java.sql.*;
import java.util.Optional;
/**
 *
 * @author Zyrus
 */
public class UserRepository {
        public Optional<UserAccount> login(String email, String password) {
        String sql = "SELECT id_user, id_hotel, nama, email, password, role FROM user WHERE email=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                UserAccount u = new UserAccount();
                u.setIdUser(rs.getInt("id_user"));
                u.setIdHotel(rs.getInt("id_hotel"));
                u.setNama(rs.getString("nama"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                return Optional.of(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import DAO.AuthDAO;
import Model.UserAccount;
import java.util.Optional;
/**
 *
 * @author Zyrus
 */
public class AuthService {
    // [DIUBAH] Ganti UserRepository menjadi AuthDAO
    private final AuthDAO dao = new AuthDAO(); 

    public Optional<UserAccount> login(String email, String password) {
        if (email == null || email.isBlank()) return Optional.empty();
        if (password == null || password.isBlank()) return Optional.empty();
        
        // [DIUBAH] Panggil metode login dari DAO yang baru
        return dao.login(email, password); 
    }
}
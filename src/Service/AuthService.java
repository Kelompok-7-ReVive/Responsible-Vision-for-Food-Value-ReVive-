/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Service;
import Model.UserAccount;
import Repository.UserRepository;
import java.util.Optional;
/**
 *
 * @author Zyrus
 */
public class AuthService {
        private final UserRepository repo = new UserRepository();

    public Optional<UserAccount> login(String email, String password) {
        if (email == null || email.isBlank()) return Optional.empty();
        if (password == null || password.isBlank()) return Optional.empty();
        return repo.login(email, password);
    }
}
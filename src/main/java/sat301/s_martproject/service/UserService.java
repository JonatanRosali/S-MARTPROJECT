package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.UserRepo;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void registerUser(String email, String username,String plainPassword) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(plainPassword));
        userRepo.save(user);
    }

    public boolean emailExists(String email) {
        return userRepo.findByEmail(email) != null;
    }

    public boolean isValidPassword(String password) {
        // Regex for password validation
        String passwordRegex = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$";
        return password.matches(passwordRegex);
    }
}

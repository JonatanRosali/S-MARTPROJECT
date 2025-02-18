package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.UserRepo;

@Service
public class AuthenticateService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticate(String email, String plainPassword) {
        User user = userRepo.findByEmail(email);
        if (user != null) {
            return passwordEncoder.matches(plainPassword, user.getPassword());
        }
        return false;
    }
}

package sat301.s_martproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import sat301.s_martproject.model.User;
import sat301.s_martproject.model.UserDetails;
import sat301.s_martproject.model.UserRole;
import sat301.s_martproject.repository.UserDetailsRepo;
import sat301.s_martproject.repository.UserRepo;
import sat301.s_martproject.repository.UserRoleRepo;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserRoleRepo userRoleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsRepo userDetailsRepo;

    public void registerUser(String email, String username, String plainPassword) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(plainPassword)); // Encrypt password
        user.setProfile_img_url("/images/blankprofile.png");
        UserRole customerRole = userRoleRepo.findById(1);
        if (customerRole == null) {
            throw new RuntimeException("Customer role not found in the database!");
        }
        user.setRole(customerRole);
        userRepo.save(user);
    }

    public boolean emailExists(String email) {
        return userRepo.findByEmail(email) != null;
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[0-9]).{8,}$");
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public void updateUser(User user) {
        userRepo.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword)); 
        userRepo.save(user);
    }
    

    public List<UserDetails> getUserShippingDetails(User user) {
        return userDetailsRepo.findByUser(user);
    }

    public void addShippingDetails(User user, String recipientName, String phoneNumber, String address, 
        String detailType, String additionalInfo, boolean isDefault) {
        UserDetails newDetails = new UserDetails(user, recipientName, detailType, phoneNumber, address, additionalInfo, isDefault);
        userDetailsRepo.save(newDetails);
    }

    public void removeExistingDefaultAddress(User user) {
        List<UserDetails> existingDefaults = userDetailsRepo.findByUserAndIsDefault(user, true);
        for (UserDetails detail : existingDefaults) {
        detail.setDefault(false); // Remove old default
        userDetailsRepo.save(detail);
        }
    }
    public UserDetails getShippingDetailById(Long id) {
        return userDetailsRepo.findById(id).orElse(null);
    }

    @Transactional
    public boolean removeShippingDetail(Long id, User user) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepo.findById(id);
    
        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
            if (userDetails!=null) { 
                userDetailsRepo.delete(userDetails);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean updateShippingDetail(Long id, User user, String recipientName, String phoneNumber, String address, String detailType, String additionalInfo, boolean isDefault) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepo.findById(id);
    
        if (userDetailsOpt.isPresent()) {
            UserDetails userDetails = userDetailsOpt.get();
    
            userDetails.setRecipient_name(recipientName);
            userDetails.setPhone_number(phoneNumber);
            userDetails.setAddress(address);
            userDetails.setDetail_type(detailType);
            userDetails.setAdditional_info(additionalInfo);
            userDetails.setDefault(isDefault);
    
            userDetailsRepo.save(userDetails); // Save changes
            return true;
        }
        return false;
    }
    public long countUsersByRole(int roleId) {
        return userRepo.countUsersByRoleId(roleId);
    }
    

}

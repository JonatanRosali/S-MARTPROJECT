package sat301.s_martproject.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.User;
import sat301.s_martproject.service.AuthenticateService;
import sat301.s_martproject.service.UserService;



@Controller
public class UserController {
    private static final String UPLOAD_DIR = "uploads/";
    @Autowired
    private AuthenticateService authenticateService;
    @GetMapping("/signin")
    public String signInPage() {
        return "signin"; 
    }

    @PostMapping("/signin")
    public String signin(String email, String password, Model model, HttpSession session) {
        if (authenticateService.authenticate(email, password)) {
            User user = userService.getUserByEmail(email);
            session.setAttribute("user", user);
            session.setAttribute("userImage", user.getProfile_img_url()); // Store in session
        return "redirect:/home"; 
    } else {
        model.addAttribute("error", "Invalid credentials!");
        return "signin";
    }
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }
    
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public String signup(String username, String email, String password, String confirmPassword, Model model, HttpSession session) {
        if (!userService.isValidPassword(password)) {
            model.addAttribute("error", "Password must be at least 8 characters long, contain at least one uppercase letter, and one number.");
            return "signup";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "signup";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email is already in use!");
            return "signup";
        }
        
        userService.registerUser(email, username, password);
        model.addAttribute("message", "User registered successfully!");

        User user = userService.getUserByEmail(email);

        session.setAttribute("user", user);
        session.setAttribute("userImage", user.getProfile_img_url());

        // Redirect to home
        return "redirect:/home"; 
    }

    @GetMapping("/account-service")
    public String accountServicePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";  // Redirect to signin if user is not logged in
        }

        model.addAttribute("user", user);
        return "accountService"; // Render the account service page
    }
    @GetMapping("/account-service/change-profile-picture")
    public String changeProfilePicture(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; // Ensure user is logged in
        }
        model.addAttribute("user", user);
        return "change-profile-picture";
    }

    @PostMapping("/account-service/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("profilePicture") MultipartFile file, HttpSession session, Model model) {
        if (!file.isEmpty()) {
            try {
                // Ensure the upload directory exists
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Get the current user
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    String currentImage = user.getProfile_img_url();

                    // Check if the current image is from /uploads/ before deleting
                    if (currentImage != null && currentImage.startsWith("/uploads/")) {
                        Path oldImagePath = Paths.get(currentImage.substring(1)).toAbsolutePath().normalize();
                        Files.deleteIfExists(oldImagePath);
                    }

                    // Save the new file with a unique name
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.write(filePath, file.getBytes());

                    // Update user's profile image path in the database
                    user.setProfile_img_url("/uploads/" + fileName);
                    userService.updateUser(user);

                    // Update session with new image path
                    session.setAttribute("userImage", "/uploads/" + fileName);
                }

                model.addAttribute("message", "Profile picture updated successfully!");
                return "redirect:/account-service"; // Redirect back to account page
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload the image.");
            }
        } else {
            model.addAttribute("error", "Please select a file to upload.");
        }
        return "change-profile-picture";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/home";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File size exceeds the maximum allowed limit!");
        return "redirect:/account-service/change-profile-picture";
    }
    
}

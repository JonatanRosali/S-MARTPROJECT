package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.User;
import sat301.s_martproject.service.AuthenticateService;
import sat301.s_martproject.service.UserService;



@Controller
public class UserController {
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/home";
    }
    
}

package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import sat301.s_martproject.service.AuthenticateService;
import sat301.s_martproject.service.UserService;



@Controller
public class UserController {
    @Autowired
    private AuthenticateService authenticateService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/signin")
    public String signInPage() {
        return "signin"; 
    }

    @PostMapping("/signin")
    public String signin(String email, String password, Model model) {
        if (authenticateService.authenticate(email, password)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
            authenticationManager.authenticate(authToken);
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
    public String signup(String username, String email, String password, String confirmPassword, Model model) {
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

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        authenticationManager.authenticate(authToken); 

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Redirect to home
        return "redirect:/home"; 
    }

    
}

package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.User;
import sat301.s_martproject.service.UserService;

@Controller
public class StaffController {
    @Autowired
    private UserService userService;

    @GetMapping("/staff-dashboard")
    public String staffDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        // Ensure user is logged in and has role_id == 2 (staff)
        if (user == null || user.getRole() == null || user.getRole().getRole_id() != 2) {
            return "redirect:/signin"; // Redirect unauthorized users to signin page
        }  

        long totalCustomers = userService.countUsersByRole(1);
        // Pass all user-related attributes through the model
        model.addAttribute("user", user);
        model.addAttribute("userImage", user.getProfile_img_url() != null ? user.getProfile_img_url() : "/images/default-profile.png");
        model.addAttribute("userRole", user.getRole().getRole_name());
        model.addAttribute("totalCustomers", totalCustomers);

        return "staff-dashboard"; // Ensure you have a Thymeleaf template for this page
    }
}

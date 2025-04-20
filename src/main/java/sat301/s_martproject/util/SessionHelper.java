package sat301.s_martproject.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import sat301.s_martproject.model.User;

public class SessionHelper {

    public static boolean isUserStaff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() != null && user.getRole().getRole_id() == 2;
    }

    public static void addUserAttributesToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRole() != null ? user.getRole().getRole_name() : "Unknown");
            model.addAttribute("userImage", user.getProfile_img_url() != null ? user.getProfile_img_url() : "/images/default-profile.png");
        }
    }
}

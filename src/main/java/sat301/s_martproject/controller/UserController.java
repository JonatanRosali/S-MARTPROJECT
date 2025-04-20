package sat301.s_martproject.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;
import sat301.s_martproject.service.*;
import sat301.s_martproject.util.SessionHelper;

@Controller
public class UserController {

    private static final String UPLOAD_DIR = "uploads/profile-pictures/";

    @Autowired private AuthenticateService authenticateService;
    @Autowired private UserService userService;
    @Autowired private OrderRepo orderRepo;
    @Autowired private OrderListRepo orderListRepo;
    @Autowired private UserDetailsRepo userDetailsRepo;

    @GetMapping("/signin")
    public String signInPage() {
        return "signin";
    }

    @PostMapping("/signin")
    public String signin(String email, String password, Model model, HttpSession session) {
        if (authenticateService.authenticate(email, password)) {
            User user = userService.getUserByEmail(email);
            session.setAttribute("user", user);
            session.setAttribute("userImage", user.getProfile_img_url());

            if (SessionHelper.isUserStaff(session)) return "redirect:/staff-dashboard";
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

    @PostMapping("/signup")
    public String signup(String username, String email, String password, String confirmPassword, Model model, HttpSession session) {
        if (!userService.isValidPassword(password)) {
            model.addAttribute("error", "Password must be at least 8 characters, include 1 uppercase and 1 number.");
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
        User user = userService.getUserByEmail(email);
        session.setAttribute("user", user);
        session.setAttribute("userImage", user.getProfile_img_url());

        return "redirect:/home";
    }

    @GetMapping("/account-service")
    public String accountServicePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        model.addAttribute("user", user);
        return "accountService";
    }

    @GetMapping("/account-service/change-profile-picture")
    public String changeProfilePicture(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        model.addAttribute("user", user);
        return "change-profile-picture";
    }

    @PostMapping("/account-service/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("profilePicture") MultipartFile file, HttpSession session, Model model) {
        if (!file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                User user = (User) session.getAttribute("user");
                if (user != null) {
                    String currentImage = user.getProfile_img_url();
                    if (currentImage != null && currentImage.startsWith("/uploads/profile-pictures/")) {
                        Path oldImagePath = Paths.get(UPLOAD_DIR, currentImage.replace("/uploads/profile-pictures/", "")).normalize();
                        Files.deleteIfExists(oldImagePath);
                    }

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.write(filePath, file.getBytes());

                    String newImagePath = "/uploads/profile-pictures/" + fileName;
                    user.setProfile_img_url(newImagePath);
                    userService.updateUser(user);
                    session.setAttribute("userImage", newImagePath);
                }

                model.addAttribute("message", "Profile picture updated successfully!");
                return "redirect:/account-service";
            } catch (IOException e) {
                model.addAttribute("error", "Failed to upload image.");
            }
        } else {
            model.addAttribute("error", "Please select a file to upload.");
        }

        return "change-profile-picture";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File size exceeds maximum limit!");
        return "redirect:/account-service/change-profile-picture";
    }

    @GetMapping("/account-service/change-password")
    public String changePasswordPage(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/signin";
        return "change-password";
    }

    @PostMapping("/account-service/update-password")
    public String updatePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        if (!userService.checkPassword(user, oldPassword)) {
            model.addAttribute("error", "Incorrect current password!");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match!");
            return "change-password";
        }

        if (!userService.isValidPassword(newPassword)) {
            model.addAttribute("error", "Password must be 8+ characters with an uppercase and number.");
            return "change-password";
        }

        userService.updatePassword(user, newPassword);
        session.invalidate();

        model.addAttribute("success", "Password changed successfully. Please log in again.");
        return "redirect:/signin";
    }

    @GetMapping("/account-service/shipping-details")
    public String shippingDetailsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        List<UserDetails> details = userService.getUserShippingDetails(user);
        model.addAttribute("userDetailsList", details);
        return "shipping-details";
    }

    @GetMapping("/account-service/add-shipping")
    public String addShippingPage(HttpSession session) {
        return session.getAttribute("user") == null ? "redirect:/signin" : "add-shipping";
    }

    @PostMapping("/account-service/add-shipping")
    public String addShippingDetails(@RequestParam String recipientName,
                                     @RequestParam String phoneNumber,
                                     @RequestParam String address,
                                     @RequestParam String detailType,
                                     @RequestParam(required = false) String additionalInfo,
                                     @RequestParam(defaultValue = "false") boolean isDefault,
                                     HttpSession session,
                                     Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        if (isDefault) userService.removeExistingDefaultAddress(user);
        userService.addShippingDetails(user, recipientName, phoneNumber, address, detailType, additionalInfo, isDefault);

        model.addAttribute("success", "Shipping details added!");
        return "redirect:/account-service/shipping-details";
    }

    @GetMapping("/account-service/remove-shipping/{id}")
    public String removeShippingDetail(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        boolean removed = userService.removeShippingDetail(id, user);
        redirectAttributes.addFlashAttribute(removed ? "success" : "error", removed ? "Shipping detail removed." : "Failed to remove shipping detail.");
        return "redirect:/account-service/shipping-details";
    }

    @GetMapping("/account-service/edit-shipping/{id}")
    public String editShippingPage(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/signin";

        Optional<UserDetails> detail = userDetailsRepo.findById(id);
        if (detail.isEmpty()) {
            model.addAttribute("error", "Shipping detail not found.");
            return "redirect:/account-service/shipping-details";
        }

        model.addAttribute("shippingDetail", detail.get());
        return "add-shipping";
    }

    @PostMapping("/account-service/edit-shipping/{id}")
    public String editShippingDetails(@PathVariable Long id,
                                      @RequestParam String recipientName,
                                      @RequestParam String phoneNumber,
                                      @RequestParam String address,
                                      @RequestParam String detailType,
                                      @RequestParam(required = false) String additionalInfo,
                                      @RequestParam(defaultValue = "false") boolean isDefault,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        boolean updated = userService.updateShippingDetail(id, user, recipientName, phoneNumber, address, detailType, additionalInfo, isDefault);
        redirectAttributes.addFlashAttribute(updated ? "success" : "error", updated ? "Shipping details updated." : "Failed to update shipping details.");

        return "redirect:/account-service/shipping-details";
    }

    @GetMapping("/account-service/order-history")
    public String viewOrderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        List<Order> orders = orderRepo.findByUser(user);
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/order-history/details/{id}")
    public String viewUserOrderDetails(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        Order order = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
        if (order.getUser().getUser_id() != user.getUser_id()) {
            return "redirect:/account-service/view-history";
        }

        List<OrderList> items = orderListRepo.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("orderItems", items);
        model.addAttribute("user", user);
        return "order-history-details";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }
}

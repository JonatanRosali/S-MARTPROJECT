package sat301.s_martproject.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Order;
import sat301.s_martproject.model.OrderList;
import sat301.s_martproject.model.User;
import sat301.s_martproject.model.UserDetails;
import sat301.s_martproject.repository.OrderListRepo;
import sat301.s_martproject.repository.OrderRepo;
import sat301.s_martproject.repository.UserDetailsRepo;
import sat301.s_martproject.service.AuthenticateService;
import sat301.s_martproject.service.UserService;



@Controller
public class UserController {
    private static final String UPLOAD_DIR = "uploads/";
    @Autowired
    private AuthenticateService authenticateService;
    @Autowired
    private UserDetailsRepo userDetailsRepo;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrderListRepo orderListRepo;

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

            if (user.getRole() != null && user.getRole().getRole_id() == 2) {
                return "redirect:/staff-dashboard"; 
            } else {
                return "redirect:/home"; 
            }
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

        return "redirect:/home"; 
    }

    @GetMapping("/account-service")
    public String accountServicePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";  
        }

        model.addAttribute("user", user);
        return "accountService"; 
    }
    @GetMapping("/account-service/change-profile-picture")
    public String changeProfilePicture(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }
        model.addAttribute("user", user);
        return "change-profile-picture";
    }

    @PostMapping("/account-service/upload-profile-picture")
    public String uploadProfilePicture(@RequestParam("profilePicture") MultipartFile file, HttpSession session, Model model) {
        if (!file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                User user = (User) session.getAttribute("user");
                if (user != null) {
                    String currentImage = user.getProfile_img_url();

                    if (currentImage != null && currentImage.startsWith("/uploads/")) {
                        Path oldImagePath = Paths.get(currentImage.substring(1)).toAbsolutePath().normalize();
                        Files.deleteIfExists(oldImagePath);
                    }

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.write(filePath, file.getBytes());

                    user.setProfile_img_url("/uploads/" + fileName);
                    userService.updateUser(user);

                    session.setAttribute("userImage", "/uploads/" + fileName);
                }

                model.addAttribute("message", "Profile picture updated successfully!");
                return "redirect:/account-service"; 
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
        session.invalidate();
        return "redirect:/home";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "File size exceeds the maximum allowed limit!");
        return "redirect:/account-service/change-profile-picture";
    }
    
    @GetMapping("/account-service/change-password")
    public String changePasswordPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }
        return "change-password";
    }

    @PostMapping("/account-service/update-password")
    public String updatePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }

        if (!userService.checkPassword(user, oldPassword)) {
            model.addAttribute("error", "Incorrect current password!");
            return "change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match!");
            return "change-password";
        }

        if (!userService.isValidPassword(newPassword)) {
            model.addAttribute("error", "Password must be at least 8 characters, include one uppercase letter and one number.");
            return "change-password";
        }

        userService.updatePassword(user, newPassword);
        session.invalidate(); 

        model.addAttribute("success", "Password changed successfully! Please log in again.");
        return "redirect:/signin"; 
    }
    @GetMapping("/account-service/shipping-details")
    public String shippingDetailsPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }

        List<UserDetails> userDetailsList = userService.getUserShippingDetails(user);
        model.addAttribute("userDetailsList", userDetailsList);
        
        return "shipping-details"; 
    }
    @GetMapping("/account-service/add-shipping")
    public String addShippingPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }
        return "add-shipping"; 
    }

    @PostMapping("/account-service/add-shipping")
    public String addShippingDetails(
            @RequestParam("recipientName") String recipientName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("detailType") String detailType,
            @RequestParam(value = "additionalInfo", required = false) String additionalInfo,
            @RequestParam(value = "isDefault", required = false, defaultValue = "false") boolean isDefault,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }
        if (isDefault) {
            userService.removeExistingDefaultAddress(user);
        }

        userService.addShippingDetails(user, recipientName, phoneNumber, address, detailType, additionalInfo, isDefault);
        model.addAttribute("success", "Shipping details added successfully!");
        return "redirect:/account-service/shipping-details"; 
    }

    @GetMapping("/account-service/remove-shipping/{id}")
    public String removeShippingDetail(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }

        boolean removed = userService.removeShippingDetail(id, user);
        if (removed) {
            redirectAttributes.addFlashAttribute("success", "Shipping detail removed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to remove shipping detail.");
        }

        return "redirect:/account-service/shipping-details"; 
    }
    
    @GetMapping("/account-service/edit-shipping/{id}")
    public String editShippingPage(@PathVariable("id") Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }
    
        Optional<UserDetails> shippingDetail = userDetailsRepo.findById(id);
    
        if (shippingDetail.isEmpty()) {
            model.addAttribute("error", "Shipping detail not found.");
            return "redirect:/account-service/shipping-details";
        }
    
        model.addAttribute("shippingDetail", shippingDetail.get());
        return "add-shipping"; 
    }
    @PostMapping("/account-service/edit-shipping/{id}")
    public String editShippingDetails(
            @PathVariable("id") Long id,
            @RequestParam("recipientName") String recipientName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("detailType") String detailType,
            @RequestParam(value = "additionalInfo", required = false) String additionalInfo,
            @RequestParam(value = "isDefault", required = false, defaultValue = "false") boolean isDefault,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin";
        }

        boolean updated = userService.updateShippingDetail(id, user, recipientName, phoneNumber, address, detailType, additionalInfo, isDefault);

        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Shipping details updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update shipping details.");
        }

        return "redirect:/account-service/shipping-details";
    }


    @GetMapping("/account-service/order-history")
    public String viewOrderHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/signin"; 
        }

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


}

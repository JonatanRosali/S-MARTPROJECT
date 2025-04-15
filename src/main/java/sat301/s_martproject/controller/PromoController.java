package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Promo;
import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.PromoRepo;
import sat301.s_martproject.service.PromoService;

import java.util.List;


@Controller
@RequestMapping("/manage-promotions")
public class PromoController{

    @Autowired
    private PromoService promoService;
    @Autowired
    private PromoRepo promoRepo;
    
    private boolean isUserStaff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() != null && user.getRole().getRole_id() == 2;
    }

    private void addUserAttributesToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRole() != null ? user.getRole().getRole_name() : "Unknown");
            model.addAttribute("userImage", user.getProfile_img_url() != null ? user.getProfile_img_url() : "/images/default-profile.png");
        }
    }

    @GetMapping
    public String managePromotions(HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin"; // Ensure user is staff

        List<Promo> promotions = promoRepo.findAll();
        model.addAttribute("promotions", promotions);
        addUserAttributesToModel(session, model);

        return "manage-promotions";
    }


    @GetMapping("/add")
    public String addPromoPage(HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";

        addUserAttributesToModel(session, model);
        return "add-promo";
    }

    @PostMapping("/add")
    public String addPromo(@RequestParam String promo_code,
                           @RequestParam double discount,
                           @RequestParam(value = "display", defaultValue = "false") boolean display,
                           @RequestParam(value = "active", defaultValue = "false") boolean active,
                           @RequestParam(value = "promo_img", required = false) MultipartFile promoImg,
                           Model model,
                           HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/signin";
    
        if (discount <= 0 || discount > 100) {
            model.addAttribute("error", "Discount must be between 1% and 100%.");
            return "add-promo";
        }
    
        String imageUrl = null;
        if (display && (promoImg == null || promoImg.isEmpty())) {
            model.addAttribute("error", "Promo image is required when display is enabled.");
            return "add-promo";
        } else if (promoImg != null && !promoImg.isEmpty()) {
            imageUrl = promoService.uploadImage(promoImg);
            if (imageUrl == null) {
                model.addAttribute("error", "Failed to upload promo image.");
                return "add-promo";
            }
        }

        Promo promo = new Promo(promo_code, discount, display, active, imageUrl);
        promoRepo.save(promo);
    
        return "redirect:/manage-promotions";
    }

    @GetMapping("/edit/{id}")
    public String editPromoPage(@PathVariable int id, HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";

        Promo promo = promoRepo.findById(id).orElse(null);
        if (promo == null) return "redirect:/manage-promotions";
    
        model.addAttribute("promo", promo);
        addUserAttributesToModel(session, model);
        return "edit-promo";
    }


    @PostMapping("/edit/{id}")
    public String editPromo(@PathVariable int id,
                            @RequestParam String promo_code,
                            @RequestParam double discount,
                            @RequestParam(required = false) MultipartFile promo_img,
                            @RequestParam(defaultValue = "false") boolean display,
                            @RequestParam(defaultValue = "false") boolean active,
                            HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";
    
        Promo promo = promoRepo.findById(id).orElse(null);
        if (promo == null) return "redirect:/manage-promotions";
    
        promo.setPromo_code(promo_code);
        promo.setDiscount(discount);
        promo.setDisplay(display);
        promo.setActive(active);
    
        if (display) {
            if (promo_img != null && !promo_img.isEmpty()) {
                if (promo.getPromo_img_url() != null) {
                    promoService.deleteImage(promo.getPromo_img_url());
                }
                String imageUrl = promoService.uploadImage(promo_img);
                if (imageUrl != null) {
                    promo.setPromo_img_url(imageUrl);
                }
            }
        } else {
            if (promo.getPromo_img_url() != null) {
                promoService.deleteImage(promo.getPromo_img_url());
                promo.setPromo_img_url(null);
            }
        }
    
        promoRepo.save(promo);
        return "redirect:/manage-promotions";
    }
    

    @GetMapping("/toggle-active/{id}")
    public String toggleActivePromo(@PathVariable int id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/signin";

        promoService.toggleActive(id);
        return "redirect:/manage-promotions";
    }

    @GetMapping("/delete/{id}")
    public String deletePromo(@PathVariable int id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/signin";

        promoService.deletePromo(id);
        return "redirect:/manage-promotions";
    }
}

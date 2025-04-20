package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Category;
import sat301.s_martproject.service.CategoryService;
import sat301.s_martproject.util.SessionHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/manage-categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String manageCategories(HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        List<Category> categories = categoryService.getAllCategories();
        Map<Integer, Long> productCounts = categoryService.getProductCountForCategories();

        model.addAttribute("categories", categories);
        model.addAttribute("productCounts", productCounts);
        SessionHelper.addUserAttributesToModel(session, model);

        return "manage-categories";
    }

    @GetMapping("/add")
    public String addCategoryPage(HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";
        SessionHelper.addUserAttributesToModel(session, model);
        return "add-category";
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam String category_name,
                              @RequestParam("category_img") MultipartFile category_img,
                              HttpSession session,
                              Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        boolean success = categoryService.addCategory(category_name, category_img);
        if (!success) {
            model.addAttribute("error", "Failed to upload image!");
            SessionHelper.addUserAttributesToModel(session, model);
            return "add-category";
        }
        return "redirect:/manage-categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryPage(@PathVariable int id, HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isEmpty()) return "redirect:/manage-categories";

        model.addAttribute("category", category.get());
        SessionHelper.addUserAttributesToModel(session, model);
        return "edit-category";
    }

    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable int id,
                               @RequestParam String category_name,
                               @RequestParam("category_img") MultipartFile category_img,
                               HttpSession session) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        categoryService.updateCategory(id, category_name, category_img);
        return "redirect:/manage-categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        try {
            categoryService.deleteCategory(id);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return manageCategories(session, model);
        }

        return "redirect:/manage-categories";
    }
}

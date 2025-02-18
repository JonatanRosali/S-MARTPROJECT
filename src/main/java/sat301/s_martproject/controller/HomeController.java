package sat301.s_martproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.Product;
import sat301.s_martproject.model.ProductImage;
import sat301.s_martproject.repository.CategoryRepo;
import sat301.s_martproject.repository.ProductImageRepo;
import sat301.s_martproject.repository.ProductRepo;

@Controller
public class HomeController {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductImageRepo productImageRepo;

    @GetMapping("/")
    public String defaultPage() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        List<Category> categories = categoryRepo.findAll(); 
        for (Category category : categories) {
            List<Product> products = productRepo.findByCategory(category);
            for (Product product : products) {
                List<ProductImage> images = productImageRepo.findByProduct(product);
                product.setImages(images); // ✅ Attach images to the product
            }
            category.setProducts(products); 
        }
        model.addAttribute("categories", categories);
        return "home"; // Map to the home.html template
    }
}

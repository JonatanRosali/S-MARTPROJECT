package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import sat301.s_martproject.model.Product;
import sat301.s_martproject.repository.ProductRepo;

@Controller
public class ProductController {
    @Autowired
    private ProductRepo productRepo;

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable int id, Model model) {
        Product product = productRepo.findById(id).orElse(null);
        model.addAttribute("product", product);
        return "productDetail";
    }
}

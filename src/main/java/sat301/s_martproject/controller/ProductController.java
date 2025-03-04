package sat301.s_martproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Cart;
import sat301.s_martproject.model.CartDetails;
import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.Product;
import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.CartDetailsRepo;
import sat301.s_martproject.repository.CartRepo;
import sat301.s_martproject.repository.CategoryRepo;
import sat301.s_martproject.repository.ProductImageRepo;
import sat301.s_martproject.repository.ProductRepo;

@Controller
public class ProductController {
    @Autowired
    private ProductRepo productRepo;
    
    @Autowired
    private ProductImageRepo productImageRepo;

    @Autowired
    private CartRepo cartRepo;
    
    @Autowired
    private CartDetailsRepo cartDetailsRepo;
    @Autowired
    private CategoryRepo categoryRepo;

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable int id, Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        List<Category> categories = categoryRepo.findAll();

        // Fetch Product Details
        Product product = productRepo.findById(id).orElse(null);
        if (product != null) {
            product.setImages(productImageRepo.findByProduct(product)); // Load product images
        }

        // ✅ Load Cart Data (Same as HomeController)
        if (user != null) {
            Cart cart = cartRepo.findByUser(user);
            if (cart != null) {
                List<CartDetails> cartItems = cartDetailsRepo.findByCart(cart);
                int cartTotal = cartItems.stream().mapToInt(CartDetails::getQuantity).sum();
                double cartTotalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                    .sum();

                model.addAttribute("cartItems", cartItems);
                model.addAttribute("cartTotal", cartTotal);
                model.addAttribute("cartTotalPrice", cartTotalPrice);
            } else {
                model.addAttribute("cartItems", null);
                model.addAttribute("cartTotal", 0);
                model.addAttribute("cartTotalPrice", 0);
            }
        } else {
            model.addAttribute("cartItems", null);
            model.addAttribute("cartTotal", 0);
            model.addAttribute("cartTotalPrice", 0);
        }
        model.addAttribute("categories", categories);
        model.addAttribute("product", product);
        return "productDetail";
    }
}

    
    


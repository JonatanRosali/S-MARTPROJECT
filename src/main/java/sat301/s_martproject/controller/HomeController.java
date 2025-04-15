package sat301.s_martproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;

@Controller
public class HomeController {
    
    @Autowired
    private CategoryRepo categoryRepo;
    
    @Autowired
    private ProductRepo productRepo;
    
    @Autowired
    private ProductImageRepo productImageRepo;
    
    @Autowired
    private CartRepo cartRepo;
    
    @Autowired
    private CartDetailsRepo cartDetailsRepo;

    @Autowired
    private PromoRepo promoRepo;

    @GetMapping("/")
    public String defaultPage() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpSession session, HttpServletRequest request) {
        User user = (User) session.getAttribute("user");

        List<Category> categories = categoryRepo.findAll();
        for (Category category : categories) {
            List<Product> products = productRepo.findByCategory(category);
            for (Product product : products) {
                product.setImages(productImageRepo.findByProduct(product));
            }
            category.setProducts(products);
        }

        List<Promo> promotions = promoRepo.findByDisplayTrue();
        model.addAttribute("promotions", promotions);

        model.addAttribute("categories", categories);
        model.addAttribute("session.user", user);
        model.addAttribute("session.userImage", session.getAttribute("userImage"));
        model.addAttribute("currentUri", request.getRequestURI());
        
        int userRoleId = (user != null && user.getRole() != null) ? user.getRole().getRole_id() : 0;
        model.addAttribute("userRoleId", userRoleId);

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

        return "home"; 
    }


    @PostMapping("/cart/add/{productId}")
    @ResponseBody
    public Map<String, Object> addToCart(@PathVariable int productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "NOT_LOGGED_IN");
            return response;
        }

        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) {
            response.put("status", "PRODUCT_NOT_FOUND");
            return response;
        }

        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            cart = new Cart(user);
            cartRepo.save(cart);
        }

        CartDetails cartItem = cartDetailsRepo.findByCartAndProduct(cart, product);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else {
            cartItem = new CartDetails(cart, product, 1);
        }
        cartDetailsRepo.save(cartItem);

        return getCartSummary(cart);
    }

    @PostMapping("/cart/remove/{productId}")
    @ResponseBody
    public Map<String, Object> removeFromCart(@PathVariable int productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();
    
        if (user == null) {
            response.put("status", "NOT_LOGGED_IN");
            return response;
        }
    
        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            response.put("status", "CART_NOT_FOUND");
            return response;
        }
    
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) {
            response.put("status", "PRODUCT_NOT_FOUND");
            return response;
        }
    
        CartDetails cartItem = cartDetailsRepo.findByCartAndProduct(cart, product);
        if (cartItem != null) {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartDetailsRepo.save(cartItem);
            } else {
                cartDetailsRepo.delete(cartItem);
            }
        }

        return getCartSummary(cart);
    }

    private Map<String, Object> getCartSummary(Cart cart) {
        Map<String, Object> response = new HashMap<>();
        List<CartDetails> cartItems = cartDetailsRepo.findByCart(cart);
        int totalItems = cartItems.stream().mapToInt(CartDetails::getQuantity).sum();
        double totalPrice = cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice()).sum();
        
        response.put("cartTotal", totalItems);
        response.put("cartTotalPrice", totalPrice);
        return response;
    }
    @GetMapping("/search")
    public String publicSearch(@RequestParam("keyword") String keyword, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
    
        // ✅ Search Products
        List<Product> results = productRepo.searchProducts(keyword);
        for (Product product : results) {
            product.setImages(productImageRepo.findByProduct(product));
        }
    
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
    
        model.addAttribute("results", results);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("session.user", user);
        model.addAttribute("session.userImage", session.getAttribute("userImage"));
        model.addAttribute("currentUri", "/search?keyword=" + keyword);
    
        return "search";
    }
    @GetMapping("/category/{id}")
    public String categoryPage(@PathVariable("id") int categoryId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        Category category = categoryRepo.findById(categoryId).orElse(null);
        if (category == null) {
            return "redirect:/home";
        }

        List<Product> products = productRepo.findByCategory(category);
        for (Product product : products) {
            product.setImages(productImageRepo.findByProduct(product));
        }

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
        model.addAttribute("category", category);
        model.addAttribute("results", products); 
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("session.user", user);
        model.addAttribute("session.userImage", session.getAttribute("userImage"));
        model.addAttribute("currentUri", "/category/" + categoryId);

        return "category"; 
    }


}

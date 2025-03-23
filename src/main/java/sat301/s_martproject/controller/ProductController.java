package sat301.s_martproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Cart;
import sat301.s_martproject.model.CartDetails;
import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.Product;
import sat301.s_martproject.model.ProductImage;
import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.CartDetailsRepo;
import sat301.s_martproject.repository.CartRepo;
import sat301.s_martproject.repository.CategoryRepo;
import sat301.s_martproject.repository.ProductImageRepo;
import sat301.s_martproject.repository.ProductRepo;
import sat301.s_martproject.service.ProductService;

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
    @Autowired
    private ProductService productService;

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable int id, Model model, HttpSession session, HttpServletRequest request) {

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
        model.addAttribute("currentUri", request.getRequestURI());
        int userRoleId = (user != null && user.getRole() != null) ? user.getRole().getRole_id() : 0;
        model.addAttribute("userRoleId", userRoleId);
        return "productDetail";
    }
    private boolean isUserStaff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() != null && user.getRole().getRole_id() == 2;
    }

    /**
     * ✅ Helper Method: Add User Data to Model
     */
    private void addUserAttributesToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRole() != null ? user.getRole().getRole_name() : "Unknown");
            model.addAttribute("userImage", user.getProfile_img_url() != null ? user.getProfile_img_url() : "/images/default-profile.png");
        }
    }

    /**
     * ✅ Display the Manage Products Page
     */
    @GetMapping("/manage-products")
    public String manageProducts(@RequestParam(value = "search", required = false) String search, HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";
    
        List<Product> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productRepo.searchProducts(search.trim());
        } else {
            products = productRepo.findAll();
        }
    
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        addUserAttributesToModel(session, model);
    
        return "manage-products";
    }
    /**
     * ✅ Show Add Product Page
     */
    @GetMapping("/manage-products/add")
    public String addProductPage(HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";

        model.addAttribute("categories", categoryRepo.findAll());
        addUserAttributesToModel(session, model);

        return "add-product";
    }

    /**
     * ✅ Handle Adding a New Product
     */
    @PostMapping("manage-products/add")
    public String addProduct(@RequestParam String product_name,
                             @RequestParam String product_description,
                             @RequestParam double price,
                             @RequestParam double review,
                             @RequestParam int quantity,
                             @RequestParam int category_id,
                             @RequestParam("product_images") MultipartFile[] product_images,
                             HttpSession session,
                             Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";
    
        Category category = categoryRepo.findById(category_id).orElse(null);
        if (category == null) {
            model.addAttribute("error", "Invalid category!");
            return "add-product";
        }
    
        Product product = new Product(product_name, product_description, price, review, quantity, category);
    
        try {
            productService.validateProduct(product); // Validate before saving
            productRepo.save(product);
            productService.saveProductImages(product, product_images);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "add-product";
        }
    
        return "redirect:/manage-products";
    }
    
    

    /**
     * ✅ Show Edit Product Page
     */
    @GetMapping("/manage-products/edit/{id}")
    public String editProductPage(@PathVariable int id, HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/manage-products";

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        addUserAttributesToModel(session, model);

        return "edit-product";
    }

    /**
     * ✅ Handle Editing a Product
     */
    @PostMapping("/manage-products/edit/{id}")
    public String editProduct(@PathVariable int id,
                            @RequestParam String product_name,
                            @RequestParam String product_description,
                            @RequestParam double price,
                            @RequestParam double review,
                            @RequestParam int quantity,
                            @RequestParam int category_id,
                            @RequestParam(required = false) Integer main_image, 
                            @RequestParam("product_images") MultipartFile[] product_images,
                            HttpSession session,
                            Model model) {
        if (!isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/manage-products";

        Category category = categoryRepo.findById(category_id).orElse(null);
        if (category == null) return "redirect:/manage-products";

        // ✅ Validate price, review, quantity
        if (price <= 0 || review < 0 || review > 5 || quantity < 0) {
            model.addAttribute("error", "Invalid product data! Price must be > 0, review 0-5, quantity >= 0.");
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepo.findAll());
            return "edit-product";
        }

        // ✅ Update product details
        product.setProduct_name(product_name);
        product.setProduct_description(product_description);
        product.setPrice(price);
        product.setReview(review);
        product.setQuantity(quantity);
        product.setCategory(category);
        productRepo.save(product);

        // ✅ Update Main Image
        if (main_image != null) {
            productService.updateMainImage(product, main_image);
        }

        // ✅ Handle New Image Uploads
        if (product_images != null) {
            productService.saveProductImages(product, product_images);
        }

        return "redirect:/manage-products";
    }
    @GetMapping("/manage-products/delete-image/{id}")
    public String deleteImage(@PathVariable int id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/signin";
        
        productService.deleteProductImage(id);
        return "redirect:/manage-products";
    }



    /**
     * ✅ Handle Product Deletion
     */
    @GetMapping("/manage-products/delete/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product != null) {
            // ✅ Delete product images from DB and server
            List<ProductImage> images = productImageRepo.findByProduct(product);
            for (ProductImage image : images) {
                productService.deleteImage(image.getProduct_img_url());
                productImageRepo.delete(image);
            }

            // ✅ Delete product
            productRepo.delete(product);
        }

        return "redirect:/manage-products";
    }
}

    
    


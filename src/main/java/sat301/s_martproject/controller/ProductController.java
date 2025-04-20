package sat301.s_martproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;
import sat301.s_martproject.service.ProductService;
import sat301.s_martproject.service.CartService;
import sat301.s_martproject.util.SessionHelper;

@Controller
public class ProductController {

    @Autowired private ProductRepo productRepo;
    @Autowired private ProductImageRepo productImageRepo;
    @Autowired private CategoryRepo categoryRepo;
    @Autowired private ProductService productService;
    @Autowired private CartService cartService;

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable int id, Model model, HttpSession session, HttpServletRequest request) {
        User user = (User) session.getAttribute("user");

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/home";

        product.setImages(productImageRepo.findByProduct(product));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("userRoleId", user != null && user.getRole() != null ? user.getRole().getRole_id() : 0);

        if (user != null) {
            model.addAllAttributes(cartService.getCartSummary(user));
        } else {
            model.addAttribute("cartItems", null);
            model.addAttribute("cartTotal", 0);
            model.addAttribute("cartTotalPrice", 0);
        }

        return "productDetail";
    }

    @GetMapping("/manage-products")
    public String manageProducts(@RequestParam(value = "search", required = false) String search, HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        List<Product> products = (search != null && !search.trim().isEmpty())
                ? productRepo.searchProducts(search.trim())
                : productRepo.findAll();

        model.addAttribute("products", products);
        model.addAttribute("search", search);
        SessionHelper.addUserAttributesToModel(session, model);

        return "manage-products";
    }

    @GetMapping("/manage-products/add")
    public String addProductPage(HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        model.addAttribute("categories", categoryRepo.findAll());
        SessionHelper.addUserAttributesToModel(session, model);
        return "add-product";
    }

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

        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        Category category = categoryRepo.findById(category_id).orElse(null);
        if (category == null) {
            model.addAttribute("error", "Invalid category!");
            return "add-product";
        }

        Product product = new Product(product_name, product_description, price, review, quantity, category);

        try {
            productService.validateProduct(product);
            productRepo.save(product);
            productService.saveProductImages(product, product_images);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryRepo.findAll());
            return "add-product";
        }

        return "redirect:/manage-products";
    }

    @GetMapping("/manage-products/edit/{id}")
    public String editProductPage(@PathVariable int id, HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/manage-products";

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());
        SessionHelper.addUserAttributesToModel(session, model);

        return "edit-product";
    }

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

        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/manage-products";

        Category category = categoryRepo.findById(category_id).orElse(null);
        if (category == null) return "redirect:/manage-products";

        if (price <= 0 || review < 0 || review > 5 || quantity < 0) {
            model.addAttribute("error", "Invalid product data! Price must be > 0, review 0-5, quantity >= 0.");
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepo.findAll());
            return "edit-product";
        }

        product.setProduct_name(product_name);
        product.setProduct_description(product_description);
        product.setPrice(price);
        product.setReview(review);
        product.setQuantity(quantity);
        product.setCategory(category);
        productRepo.save(product);

        if (main_image != null) {
            productService.updateMainImage(product, main_image);
        }

        if (product_images != null) {
            productService.saveProductImages(product, product_images);
        }

        return "redirect:/manage-products";
    }

    @GetMapping("/manage-products/delete-image/{id}")
    public String deleteImage(@PathVariable int id, HttpSession session) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";
        productService.deleteProductImage(id);
        return "redirect:/manage-products";
    }

    @GetMapping("/manage-products/delete/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        Product product = productRepo.findById(id).orElse(null);
        if (product != null) {
            List<ProductImage> images = productImageRepo.findByProduct(product);
            for (ProductImage image : images) {
                productService.deleteImage(image.getProduct_img_url());
                productImageRepo.delete(image);
            }

            productRepo.delete(product);
        }

        return "redirect:/manage-products";
    }
}

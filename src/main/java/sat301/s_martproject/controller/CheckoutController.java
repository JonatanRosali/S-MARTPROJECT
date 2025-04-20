package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;
import sat301.s_martproject.service.*;

import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;
    @Autowired
    private PromoService promoService;
    @Autowired
    private CurrencyConversionService conversionService;
    @Autowired
    private UserDetailsRepo userDetailsRepo;
    @Autowired
    private PaymentTypeRepo paymentTypeRepo;
    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        List<UserDetails> shippingList = userDetailsRepo.findByUser(user);
        List<PaymentType> paymentTypes = paymentTypeRepo.findAll();
        List<CartDetails> cartItems = cartService.getCartItems(user);
        double total = cartService.calculateCartTotal(cartItems);

        Long defaultShippingId = shippingList.stream()
                .filter(UserDetails::isDefault)
                .map(UserDetails::getUser_details_id)
                .findFirst()
                .orElse(null);

        model.addAttribute("shippingList", shippingList);
        model.addAttribute("paymentTypes", paymentTypes);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotalPrice", total);
        model.addAttribute("defaultShippingId", defaultShippingId);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam Long shippingId,
                                  @RequestParam Integer paymentTypeId,
                                  @RequestParam(required = false) String promoCode,
                                  HttpSession session,
                                  Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        List<CartDetails> cartItems = cartService.getCartItems(user);
        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return checkoutPage(session, model);
        }

        UserDetails shippingDetails = userDetailsRepo.findById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid shipping ID"));

        PaymentType paymentType = paymentTypeRepo.findById(paymentTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment type"));

        Promo promo = promoService.getPromoByCode(promoCode).orElse(null);
        double subtotal = cartService.calculateCartTotal(cartItems);
        double discount = promo != null ? promoService.calculateDiscount(promo, subtotal) : 0;

        Order order = orderService.createOrder(user, cartItems, shippingDetails, paymentType, promo, subtotal, discount);
        cartService.clearCart(user);

        return "redirect:/order/confirmation/" + order.getOrder_id();
    }

    @GetMapping("/order/confirmation/{id}")
    public String showConfirmation(@PathVariable Long id,
                                   @RequestParam(name = "currency", defaultValue = "CNY") String currency,
                                   Model model) {

        Order order = orderService.getOrderById(id);
        double totalAmount = order.getTotal_amount();
        double convertedTotal = conversionService.convertCurrency(currency, totalAmount);

        model.addAttribute("orderId", order.getOrder_id());
        model.addAttribute("orderTotal", String.format("%.2f", totalAmount));
        model.addAttribute("convertedTotal", convertedTotal);
        model.addAttribute("currency", currency);

        return "order-confirmation";
    }

    @ResponseBody
    @PostMapping("/validate-promo")
    public Map<String, Object> validatePromo(@RequestParam String code, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> response = new HashMap<>();

        List<CartDetails> cartItems = cartService.getCartItems(user);
        double subtotal = cartService.calculateCartTotal(cartItems);

        Optional<Promo> promoOpt = promoService.getPromoByCode(code.trim());

        if (promoOpt.isPresent()) {
            Promo promo = promoOpt.get();
            double discount = promoService.calculateDiscount(promo, subtotal);
            double finalTotal = subtotal - discount;

            response.put("valid", true);
            response.put("discount", discount);
            response.put("finalTotal", finalTotal);
        } else {
            response.put("valid", false);
            response.put("discount", 0.0);
            response.put("finalTotal", subtotal);
        }

        return response;
    }
}

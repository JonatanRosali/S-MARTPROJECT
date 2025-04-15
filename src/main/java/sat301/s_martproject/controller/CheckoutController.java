package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;
import sat301.s_martproject.service.CartService;
import sat301.s_martproject.service.CurrencyConversionService;
import sat301.s_martproject.service.PromoService;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Controller
public class CheckoutController {

    @Autowired 
    private OrderRepo orderRepo;
    @Autowired
    private OrderListRepo orderListRepo;
    @Autowired
    private OrderStatusRepo orderStatusRepo;
    @Autowired 
    private UserDetailsRepo userDetailsRepo;
    @Autowired 
    private PaymentTypeRepo paymentTypeRepo;
    @Autowired
    private CartService cartService;
    @Autowired
    private PromoService promoService;
    @Autowired
    private CurrencyConversionService conversionService;


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
    public String processCheckout(
            @RequestParam Long shippingId,
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

        Promo appliedPromo = promoService.getPromoByCode(promoCode).orElse(null);
        double subtotal = cartService.calculateCartTotal(cartItems);
        double discount = appliedPromo != null ? promoService.calculateDiscount(appliedPromo, subtotal) : 0;
        double baseTotal = subtotal - discount;
        double finalTotal;
        int attempts = 0;
        do {
            int randomCents = new Random().nextInt(20) + 1; 
            finalTotal = baseTotal + (randomCents / 100.0);
            finalTotal = new BigDecimal(finalTotal).setScale(2, RoundingMode.HALF_UP).doubleValue();
            attempts++;
            if (attempts > 20) break; 
        } while (orderRepo.existsDuplicateTotal(finalTotal, 1)); 


        Order order = new Order();
        order.setOrder_date(LocalDateTime.now());
        order.setPayment_date(LocalDateTime.now());
        order.setTotal_amount(finalTotal);
        order.setUser(user);
        order.setUserDetails(shippingDetails);
        order.setPayment(paymentType);
        order.setPromo(appliedPromo);
        order.setStatus(orderStatusRepo.findById(1).orElse(null)); 

        order = orderRepo.save(order);

        for (CartDetails item : cartItems) {
            OrderList ol = new OrderList();
            ol.setOrder(order);
            ol.setProduct(item.getProduct());
            ol.setQuantity(item.getQuantity());
            ol.setPrice_at_purchase(item.getProduct().getPrice());
            orderListRepo.save(ol);
        }

        cartService.clearCart(user);

        model.addAttribute("orderId", order.getOrder_id());
        return "redirect:/order/confirmation/" + order.getOrder_id();

    }

    @GetMapping("/order/confirmation/{id}")
    public String showConfirmation(
            @PathVariable Long id,
            @RequestParam(name = "currency", defaultValue = "CNY") String currency,  // Default to CNY
            Model model) {

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));

        double totalAmount = order.getTotal_amount();
        double convertedTotal = conversionService.convertCurrency(currency, totalAmount); // Convert from CNY

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

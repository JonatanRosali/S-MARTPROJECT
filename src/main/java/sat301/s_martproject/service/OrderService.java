package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrderListRepo orderListRepo;
    @Autowired
    private OrderStatusRepo orderStatusRepo;

    public Order createOrder(User user, List<CartDetails> cartItems, UserDetails shipping, PaymentType payment, Promo promo, double subtotal, double discount) {
        double baseTotal = subtotal - discount;
        double finalTotal = generateUniqueTotal(baseTotal);

        Order order = new Order();
        order.setOrder_date(LocalDateTime.now());
        order.setPayment_date(LocalDateTime.now());
        order.setTotal_amount(finalTotal);
        order.setUser(user);
        order.setUserDetails(shipping);
        order.setPayment(payment);
        order.setPromo(promo);
        order.setStatus(orderStatusRepo.findById(1).orElse(null)); // Pending

        order = orderRepo.save(order);

        for (CartDetails item : cartItems) {
            OrderList ol = new OrderList();
            ol.setOrder(order);
            ol.setProduct(item.getProduct());
            ol.setQuantity(item.getQuantity());
            ol.setPrice_at_purchase(item.getProduct().getPrice());
            orderListRepo.save(ol);
        }

        return order;
    }

    private double generateUniqueTotal(double baseTotal) {
        int attempts = 0;
        double finalTotal;

        do {
            int randomCents = new Random().nextInt(20) + 1;
            finalTotal = baseTotal + (randomCents / 100.0);
            finalTotal = new BigDecimal(finalTotal).setScale(2, RoundingMode.HALF_UP).doubleValue();
            attempts++;
        } while (orderRepo.existsDuplicateTotal(finalTotal, 1) && attempts <= 20);

        return finalTotal;
    }

    public Order getOrderById(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
    }

    public List<String> getInsufficientStockItems(List<OrderList> orderItems) {
        List<String> insufficientStock = new ArrayList<>();
        for (OrderList item : orderItems) {
            int currentStock = item.getProduct().getQuantity();
            if (currentStock < item.getQuantity()) {
                insufficientStock.add(item.getProduct().getProduct_name());
            }
        }
        return insufficientStock;
    }

    public void reduceStock(List<OrderList> orderItems) {
        for (OrderList item : orderItems) {
            item.getProduct().setQuantity(item.getProduct().getQuantity() - item.getQuantity());
        }
    }

    public void updateOrderStatus(Order order, int statusId) {
        order.setStatus(orderStatusRepo.findById(statusId).orElse(null));
        orderRepo.save(order);
    }
}

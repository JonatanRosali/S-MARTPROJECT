package sat301.s_martproject.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.model.Order;
import sat301.s_martproject.model.OrderList;
import sat301.s_martproject.model.User;
import sat301.s_martproject.repository.*;

@Controller
@RequestMapping("/manage-orders")
public class OrderController {

    @Autowired 
    private OrderRepo orderRepo;
    @Autowired 
    private OrderStatusRepo orderStatusRepo;
    @Autowired 
    private OrderListRepo orderListRepo;

    private boolean isUserStaff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getRole() != null && user.getRole().getRole_id() == 2;
    }

    private void addUserAttributesToModel(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRole() != null ? user.getRole().getRole_name() : "Unknown");
            model.addAttribute("userImage", user.getProfile_img_url() != null ? user.getProfile_img_url() : "/images/default-profile.png");
        }
    }

    @GetMapping
    public String viewOrders(@RequestParam(defaultValue = "1") int status, Model model, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/home";
    
        List<Order> orders = orderRepo.findByStatus_Status_id(status);
        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status);
    
        Object stockError = session.getAttribute("stockError");
        if (stockError != null) {
            model.addAttribute("stockError", stockError);
            session.removeAttribute("stockError");
        }
    
        addUserAttributesToModel(session, model);
        return "manage-orders";
    }
    
    
    @GetMapping("/details/{id}")
    public String viewOrderDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/home";
    
        Order order = orderRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid order ID"));
        List<OrderList> items = orderListRepo.findByOrder(order);
    
        addUserAttributesToModel(session, model);
        model.addAttribute("order", order);
        model.addAttribute("orderItems", items);
        return "order-details";
    }
    

    @PostMapping("/confirm/{id}")
    public String confirmOrder(@PathVariable Long id, HttpSession session, Model model) {
        if (!isUserStaff(session)) return "redirect:/home";

        Order order = orderRepo.findById(id).orElseThrow();
        List<OrderList> orderItems = orderListRepo.findByOrder(order);

        List<String> insufficientStock = new ArrayList<>();

        for (OrderList item : orderItems) {
            int currentStock = item.getProduct().getQuantity();
            int orderedQuantity = item.getQuantity();
            if (currentStock < orderedQuantity) {
                insufficientStock.add(item.getProduct().getProduct_name());
            }
        }

        if (!insufficientStock.isEmpty()) {
            session.setAttribute("stockError", "Insufficient stock! Please replenish for: " + String.join(", ", insufficientStock));
            return "redirect:/manage-orders?status=1";
        }

        // Reduce stock
        for (OrderList item : orderItems) {
            item.getProduct().setQuantity(item.getProduct().getQuantity() - item.getQuantity());
        }

        order.setStatus(orderStatusRepo.findById(2).orElse(null));
        orderRepo.save(order);

        return "redirect:/manage-orders?status=2";
    }


    @PostMapping("/deliver/{id}")
    public String deliverOrder(@PathVariable Long id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/home";

        Order order = orderRepo.findById(id).orElseThrow();
        order.setStatus(orderStatusRepo.findById(3).orElse(null)); 
        orderRepo.save(order);
        return "redirect:/manage-orders?status=3";
    }

    @PostMapping("/complete/{id}")
    public String completeOrder(@PathVariable Long id, HttpSession session) {
        if (!isUserStaff(session)) return "redirect:/home";

        Order order = orderRepo.findById(id).orElseThrow();
        order.setStatus(orderStatusRepo.findById(4).orElse(null)); 
        orderRepo.save(order);
        return "redirect:/manage-orders?status=4";
    }
}

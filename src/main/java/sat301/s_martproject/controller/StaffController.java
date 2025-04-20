package sat301.s_martproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import sat301.s_martproject.repository.OrderRepo;
import sat301.s_martproject.service.UserService;
import sat301.s_martproject.util.SessionHelper;

@Controller
public class StaffController {

    @Autowired private UserService userService;
    @Autowired private OrderRepo orderRepo;

    @GetMapping("/staff-dashboard")
    public String staffDashboard(HttpSession session, Model model) {
        if (!SessionHelper.isUserStaff(session)) return "redirect:/signin";

        long totalCustomers = userService.countUsersByRole(1);
        long totalSales = orderRepo.countByStatusNot(1);
        double totalSalesValue = orderRepo.sumSalesByStatusNot(1);
        double avgTransactionValue = orderRepo.avgTransactionByStatusNot(1);

        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalSalesValue", totalSalesValue);
        model.addAttribute("avgTransactionValue", avgTransactionValue);

        SessionHelper.addUserAttributesToModel(session, model);
        return "staff-dashboard";
    }
}

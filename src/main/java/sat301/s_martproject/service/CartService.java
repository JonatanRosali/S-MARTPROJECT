package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sat301.s_martproject.model.*;
import sat301.s_martproject.repository.*;


import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartDetailsRepo cartDetailsRepo;



    public Cart getUserCart(User user) {
        return cartRepo.findByUser(user);
    }

    public List<CartDetails> getCartItems(User user) {
        Cart cart = cartRepo.findByUser(user);
        if (cart != null) {
            return cartDetailsRepo.findByCart(cart);
        }
        return List.of(); // empty
    }

    public double calculateCartTotal(List<CartDetails> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
    }
    public void clearCart(User user) {
        Cart cart = cartRepo.findByUser(user);
        if (cart != null) {
            List<CartDetails> cartItems = cartDetailsRepo.findByCart(cart);
            cartDetailsRepo.deleteAll(cartItems);
        }
    }
    
}

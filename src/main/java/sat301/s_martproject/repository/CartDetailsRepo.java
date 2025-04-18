package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Cart;
import sat301.s_martproject.model.CartDetails;
import sat301.s_martproject.model.Product;

import java.util.List;

@Repository
public interface CartDetailsRepo extends JpaRepository<CartDetails, Long> {
    List<CartDetails> findByCart(Cart cart);
    CartDetails findByCartAndProduct(Cart cart, Product product); 
    void deleteByCart(Cart cart); 
}

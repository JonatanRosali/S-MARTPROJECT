package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Cart;
import sat301.s_martproject.model.User;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {
    Cart findByUser(User user); 
}

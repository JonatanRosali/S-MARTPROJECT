package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Order;
import sat301.s_martproject.model.OrderList;

import java.util.List;

@Repository
public interface OrderListRepo extends JpaRepository<OrderList, Long> {
    
    List<OrderList> findByOrder(Order order);
}

package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Order;
import sat301.s_martproject.model.User;

import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user); // ✅ simple and correct

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.total_amount = :amount AND o.status.status_id = :statusId")
    boolean existsDuplicateTotal(@Param("amount") double amount, @Param("statusId") int statusId);

    @Query("SELECT o FROM Order o WHERE o.status.status_id = :statusId ORDER BY o.order_date DESC")
    List<Order> findByStatus_Status_id(@Param("statusId") int statusId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status.status_id <> :statusId")
    long countByStatusNot(@Param("statusId") int statusId);


    @Query("SELECT COALESCE(SUM(o.total_amount), 0) FROM Order o WHERE o.status.status_id <> :status")
    double sumSalesByStatusNot(int status);

    @Query("SELECT COALESCE(AVG(o.total_amount), 0) FROM Order o WHERE o.status.status_id <> :status")
    double avgTransactionByStatusNot(int status);

}

package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.PaymentType;

@Repository
public interface PaymentTypeRepo extends JpaRepository<PaymentType, Integer> {
}

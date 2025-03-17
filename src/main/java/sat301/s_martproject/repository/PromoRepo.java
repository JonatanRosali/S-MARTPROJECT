package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Promo;

import java.util.List;

@Repository
public interface PromoRepo extends JpaRepository<Promo, Integer> {
    
    List<Promo> findByActiveTrue();

    List<Promo> findByDisplayTrue();
}

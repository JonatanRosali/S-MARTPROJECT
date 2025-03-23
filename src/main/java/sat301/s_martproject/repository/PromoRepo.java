package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.Promo;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoRepo extends JpaRepository<Promo, Integer> {
    
    List<Promo> findByActiveTrue();

    List<Promo> findByDisplayTrue();

    @Query("SELECT p FROM Promo p WHERE p.promo_code = :code")
    Optional<Promo> findByPromo_Code(@Param("code") String code);

}

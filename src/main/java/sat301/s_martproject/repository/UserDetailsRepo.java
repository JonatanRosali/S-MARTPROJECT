package sat301.s_martproject.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sat301.s_martproject.model.User;
import sat301.s_martproject.model.UserDetails;

@Repository
public interface UserDetailsRepo extends JpaRepository<UserDetails, Long> {
    List<UserDetails> findByUser(User user);
    List<UserDetails> findByUserAndIsDefault(User user, boolean isDefault);
}

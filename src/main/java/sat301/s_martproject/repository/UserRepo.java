package sat301.s_martproject.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import sat301.s_martproject.model.User;

public interface UserRepo extends JpaRepository<User, Integer>{
    User findByEmail(String email);
}

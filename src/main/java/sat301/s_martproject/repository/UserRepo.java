package sat301.s_martproject.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sat301.s_martproject.model.User;

public interface UserRepo extends JpaRepository<User, Integer>{
    User findByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.role_id = :roleId") 
    long countUsersByRoleId(@Param("roleId") int roleId);
}

package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sat301.s_martproject.model.UserRole;



public interface UserRoleRepo extends JpaRepository<UserRole, Integer>{
    UserRole findById(int role_id);
}

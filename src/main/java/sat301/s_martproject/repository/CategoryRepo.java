package sat301.s_martproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sat301.s_martproject.model.Category;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    
}

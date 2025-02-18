package sat301.s_martproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.Product;

public interface ProductRepo extends JpaRepository<Product, Integer>{
    List<Product> findByCategory(Category category);
}

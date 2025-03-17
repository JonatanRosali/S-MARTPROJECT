package sat301.s_martproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.Product;

public interface ProductRepo extends JpaRepository<Product, Integer>{
    List<Product> findByCategory(Category category);
    @Query("SELECT p FROM Product p WHERE LOWER(p.product_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.category.category_name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

}

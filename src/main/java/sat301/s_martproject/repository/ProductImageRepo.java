package sat301.s_martproject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sat301.s_martproject.model.Product;
import sat301.s_martproject.model.ProductImage;

@Repository
public interface ProductImageRepo extends JpaRepository<ProductImage, Integer>{
    List<ProductImage> findByProduct(Product product);
}

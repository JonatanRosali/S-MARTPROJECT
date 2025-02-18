package sat301.s_martproject.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import sat301.s_martproject.model.Category;
import sat301.s_martproject.repository.CategoryRepo;

import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepo categoryRepo) {
        return args -> {
            if (categoryRepo.count() == 0) { // Only insert if DB is empty
                List<Category> categories = List.of(
                        new Category("Dairy", "/images/CategoryDairy.png"),
                        new Category("Meats", "/images/CategoryMeats.png"),
                        new Category("Snacks", "/images/CategorySnacks.png"),
                        new Category("Beverages", "/images/CategoryBeverages.png"),
                        new Category("Produce", "/images/CategoryProduce.png"),
                        new Category("Bakery", "/images/CategoryBakery.png")
                );
                categoryRepo.saveAll(categories);
            } else {
            }
        };
    }
}

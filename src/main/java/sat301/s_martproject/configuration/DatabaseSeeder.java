package sat301.s_martproject.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sat301.s_martproject.model.Category;
import sat301.s_martproject.model.PaymentType;
import sat301.s_martproject.model.UserRole;
import sat301.s_martproject.repository.CategoryRepo;
import sat301.s_martproject.repository.PaymentTypeRepo;
import sat301.s_martproject.repository.UserRoleRepo;

import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepo categoryRepo, UserRoleRepo userRoleRepo, PaymentTypeRepo paymentTypeRepo) {
        return args -> {
            // Seed user_role table
            if (userRoleRepo.count() == 0) { // Only insert if empty
                List<UserRole> roles = List.of(
                        new UserRole(1,"Customer", "Customer purchase roles"),
                        new UserRole(2,"Staff", "Staff store management role"),
                        new UserRole(3,"Admin", "Management role")
                );
                userRoleRepo.saveAll(roles);
            }

            // Seed category table
            if (categoryRepo.count() == 0) { // Only insert if empty
                List<Category> categories = List.of(
                        new Category("Dairy", "/images/CategoryDairy.png"),
                        new Category("Meats", "/images/CategoryMeats.png"),
                        new Category("Snacks", "/images/CategorySnacks.png"),
                        new Category("Beverages", "/images/CategoryBeverages.png"),
                        new Category("Produce", "/images/CategoryProduce.png"),
                        new Category("Bakery", "/images/CategoryBakery.png")
                );
                categoryRepo.saveAll(categories);
            }

            if (paymentTypeRepo.count() == 0) { // Only insert if empty
                List<PaymentType> types = List.of(
                        new PaymentType("WeChat", "Wechat application payment"),
                        new PaymentType("Alipay", "Alipay application payment"),
                        new PaymentType("Bank Transfer", "Bank transfer payment")
                );
                paymentTypeRepo.saveAll(types);
            }
        };
    }
}

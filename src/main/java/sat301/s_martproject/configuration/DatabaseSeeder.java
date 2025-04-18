package sat301.s_martproject.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import sat301.s_martproject.model.OrderStatus;
import sat301.s_martproject.model.PaymentType;
import sat301.s_martproject.model.User;
import sat301.s_martproject.model.UserRole;
import sat301.s_martproject.repository.CategoryRepo;
import sat301.s_martproject.repository.OrderStatusRepo;
import sat301.s_martproject.repository.PaymentTypeRepo;
import sat301.s_martproject.repository.UserRepo;
import sat301.s_martproject.repository.UserRoleRepo;

import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepo categoryRepo, UserRoleRepo userRoleRepo, PaymentTypeRepo paymentTypeRepo, OrderStatusRepo orderStatusRepo, UserRepo userRepo) {
        return args -> {

            if (userRoleRepo.count() == 0) { 
                List<UserRole> roles = List.of(
                        new UserRole(1,"Customer", "Customer purchase roles"),
                        new UserRole(2,"Staff", "Staff store management role")
                );
                userRoleRepo.saveAll(roles);
            }

            if (paymentTypeRepo.count() == 0) {
                List<PaymentType> types = List.of(
                        new PaymentType("WeChat", "Wechat application payment"),
                        new PaymentType("Alipay", "Alipay application payment"),
                        new PaymentType("Bank Transfer", "Bank transfer payment")
                );
                paymentTypeRepo.saveAll(types);
            }
            if (orderStatusRepo.count() == 0) {
                List<OrderStatus> statuses = List.of(
                        new OrderStatus("Pending"),
                        new OrderStatus("Confirmed"),
                        new OrderStatus("Delivered"),
                        new OrderStatus("Completed")
                );
                orderStatusRepo.saveAll(statuses);
            };
            if (userRepo.count() == 0) {
                UserRole adminRole = userRoleRepo.findById(2); 
                if (adminRole != null) {
                    List<User> users = List.of(
                        new User("Admin1", "admin1@smart.com",
                            new BCryptPasswordEncoder().encode("Admin1234"), adminRole)
                    );
                    userRepo.saveAll(users);
                }
            }
        };
    }
}

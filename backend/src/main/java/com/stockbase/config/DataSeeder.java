package com.stockbase.config;

import com.stockbase.model.*;
import com.stockbase.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // already seeded

        // Create admin user
        User admin = User.builder()
            .fullName("Admin User")
            .email("admin@stockbase.com")
            .password(passwordEncoder.encode("admin123"))
            .role(User.Role.ADMIN)
            .build();
        userRepository.save(admin);

        // Create regular user
        User user = User.builder()
            .fullName("Regular User")
            .email("user@stockbase.com")
            .password(passwordEncoder.encode("user123"))
            .role(User.Role.USER)
            .build();
        userRepository.save(user);

        // Categories
        Category office = categoryRepository.save(Category.builder().name("Office Supplies").description("Pens, paper, notebooks").build());
        Category electronics = categoryRepository.save(Category.builder().name("Electronics").description("Devices and accessories").build());
        Category furniture = categoryRepository.save(Category.builder().name("Furniture").description("Desks, chairs, shelving").build());
        Category apparel = categoryRepository.save(Category.builder().name("Apparel").description("Clothing and uniforms").build());

        // Suppliers
        Supplier acme = supplierRepository.save(Supplier.builder().name("Acme Wholesale").contactEmail("orders@acme.com").phone("405-555-0101").build());
        Supplier pacific = supplierRepository.save(Supplier.builder().name("Pacific Goods Co.").contactEmail("supply@pacgood.com").phone("405-555-0202").build());
        Supplier delta = supplierRepository.save(Supplier.builder().name("Delta Distributors").contactEmail("info@deltadist.com").phone("405-555-0303").build());

        // Products
        List<Product> products = List.of(
            Product.builder().name("Blue Pen Pack (12ct)").sku("OFF-001").price(new BigDecimal("4.99")).quantity(250).reorderThreshold(50).category(office).supplier(acme).build(),
            Product.builder().name("Notebook A5").sku("OFF-002").price(new BigDecimal("3.49")).quantity(400).reorderThreshold(60).category(office).supplier(acme).build(),
            Product.builder().name("Sticky Notes (5-pack)").sku("OFF-003").price(new BigDecimal("2.99")).quantity(600).reorderThreshold(80).category(office).supplier(acme).build(),
            Product.builder().name("Wireless Mouse").sku("ELE-001").price(new BigDecimal("29.99")).quantity(18).reorderThreshold(20).category(electronics).supplier(pacific).build(),
            Product.builder().name("USB-C Hub (7-in-1)").sku("ELE-002").price(new BigDecimal("49.99")).quantity(7).reorderThreshold(15).category(electronics).supplier(pacific).build(),
            Product.builder().name("Laptop Stand").sku("ELE-003").price(new BigDecimal("44.99")).quantity(14).reorderThreshold(10).category(electronics).supplier(pacific).build(),
            Product.builder().name("Desk Lamp").sku("FUR-001").price(new BigDecimal("39.99")).quantity(0).reorderThreshold(10).category(furniture).supplier(delta).build(),
            Product.builder().name("Ergonomic Chair").sku("FUR-002").price(new BigDecimal("299.00")).quantity(5).reorderThreshold(8).category(furniture).supplier(delta).build(),
            Product.builder().name("Monitor Stand").sku("FUR-003").price(new BigDecimal("59.99")).quantity(22).reorderThreshold(10).category(furniture).supplier(delta).build(),
            Product.builder().name("Polo Shirt (S)").sku("APP-001").price(new BigDecimal("19.99")).quantity(80).reorderThreshold(20).category(apparel).supplier(acme).build(),
            Product.builder().name("Polo Shirt (M)").sku("APP-002").price(new BigDecimal("19.99")).quantity(6).reorderThreshold(20).category(apparel).supplier(acme).build(),
            Product.builder().name("Polo Shirt (L)").sku("APP-003").price(new BigDecimal("19.99")).quantity(90).reorderThreshold(20).category(apparel).supplier(acme).build()
        );
        productRepository.saveAll(products);

        System.out.println("✅ Seed data loaded.");
        System.out.println("   Admin: admin@stockbase.com / admin123");
        System.out.println("   User:  user@stockbase.com  / user123");
    }
}

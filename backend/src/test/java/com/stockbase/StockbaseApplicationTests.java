package com.stockbase;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the full Spring application context (JPA, security,
 * seeding, all beans) starts without error against the H2 test datasource.
 */
@SpringBootTest
class StockbaseApplicationTests {

    @Test
    void contextLoads() {
    }
}

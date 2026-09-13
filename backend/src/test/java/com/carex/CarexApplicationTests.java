package com.carex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring Boot application context loads successfully,
 * which includes Hibernate entity scanning and schema validation against PostgreSQL.
 */
@SpringBootTest
class CarexApplicationTests {

    @Test
    void contextLoads() {
        // If context loads without exceptions, all JPA entity mappings are valid.
    }
}

package com.irctc.cugDirectory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.irctc.cugDirectory.loader.AdminAccountSeeder;

/**
 * Verifies that the Spring application context loads without errors.
 * Uses H2 in-memory database (see src/test/resources/application.properties).
 * AdminAccountSeeder is mocked to prevent DB seeding during tests.
 * ExcelDataLoader no longer needs mocking — it is disabled (no @Component).
 */
@SpringBootTest
class CugDirectoryApplicationTests {

    @MockBean
    AdminAccountSeeder adminAccountSeeder;

    @Test
    void contextLoads() {
        // Passes if the Spring context starts up without any exceptions.
    }
}

package com.miniapp.foodshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "logging.request-response.enabled=true",
    "logging.request-response.max-body-length=1000",
    "logging.request-response.mask-sensitive-data=true"
})
class RequestResponseLoggingFilterTest {

    @Test
    void contextLoads() {
        // Test that the Spring context loads with the logging filter
        assertTrue(true);
    }
}

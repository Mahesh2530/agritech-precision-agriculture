package com.agritech.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void loginReturnsFullNameForFrontendCompatibility() {
        AuthService authService = new AuthService();

        ResponseEntity<Map<String, Object>> response = authService.login("admin@agritech.dev", "admin123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Admin User", response.getBody().get("fullName"));
        assertEquals("ADMIN", response.getBody().get("role"));
    }
}

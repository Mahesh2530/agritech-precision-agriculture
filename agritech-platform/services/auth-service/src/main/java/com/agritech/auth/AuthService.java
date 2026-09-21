package com.agritech.auth;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public AuthService() {
        users.put("admin@agritech.dev", new User("admin@agritech.dev", "admin123", "Admin User", Role.ADMIN));
    }

    public ResponseEntity<Map<String, Object>> login(String email, String password) {
        User user = users.get(email);
        if (user == null || !user.password().equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", "demo-jwt-token-for-" + email);
        result.put("email", user.email());
        result.put("fullName", user.fullName());
        result.put("role", user.role().name());
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Map<String, Object>> register(AuthDtos.RegisterRequest request) {
        User user = new User(request.email(), request.password(), request.fullName(), request.role() != null ? request.role() : Role.VIEWER);
        users.put(user.email(), user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", "demo-jwt-token-for-" + user.email());
        result.put("email", user.email());
        result.put("fullName", user.fullName());
        result.put("role", user.role().name());
        return ResponseEntity.ok(result);
    }
}

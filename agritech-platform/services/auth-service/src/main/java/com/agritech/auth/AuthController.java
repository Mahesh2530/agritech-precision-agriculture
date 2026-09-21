package com.agritech.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService = new AuthService();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }
}

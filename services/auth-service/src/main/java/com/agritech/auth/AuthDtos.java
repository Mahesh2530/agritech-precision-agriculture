package com.agritech.auth;

public class AuthDtos {
    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String fullName, Role role) {}
}

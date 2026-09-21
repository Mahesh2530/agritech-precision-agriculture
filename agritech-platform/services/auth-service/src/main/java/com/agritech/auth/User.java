package com.agritech.auth;

public record User(String email, String password, String fullName, Role role) {
}

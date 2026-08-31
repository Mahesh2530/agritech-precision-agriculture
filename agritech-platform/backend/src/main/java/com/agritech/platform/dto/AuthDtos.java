package com.agritech.platform.dto;

import com.agritech.platform.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record RegisterRequest(@Email @NotBlank String email, @NotBlank String password,
                                   @NotBlank String fullName, Role role, Long organizationId) {}

    public record AuthResponse(String token, String email, String fullName, Role role) {}
}

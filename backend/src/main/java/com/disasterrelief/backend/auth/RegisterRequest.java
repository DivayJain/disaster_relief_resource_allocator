package com.disasterrelief.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * POST /api/auth/register body. Public sign-up can only create CITIZEN or VOLUNTEER accounts; privileged roles
 * are assigned by an administrator. A missing role defaults to CITIZEN.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 200) String password,
        Role role) {
}

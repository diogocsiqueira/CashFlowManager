package com.diogodev.caixa.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record MobileLogoutRequest(
        @NotBlank String refreshToken
) {}
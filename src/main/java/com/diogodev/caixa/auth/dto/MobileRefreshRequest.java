package com.diogodev.caixa.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record MobileRefreshRequest(
        @NotBlank String refreshToken
) {}
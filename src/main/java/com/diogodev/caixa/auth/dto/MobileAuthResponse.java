package com.diogodev.caixa.auth.dto;

public record MobileAuthResponse(
        String accessToken,
        String refreshToken
) {}
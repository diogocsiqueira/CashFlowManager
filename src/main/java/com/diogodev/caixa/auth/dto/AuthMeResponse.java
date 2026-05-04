package com.diogodev.caixa.auth.dto;

import java.util.Set;

public record AuthMeResponse(
        Long id,
        String email,
        Set<String> roles
) {}
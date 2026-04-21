package com.diogodev.caixa.category.dto;

public record CategoryResponse(
        Long id,
        String name,
        boolean isDefault
) {
}
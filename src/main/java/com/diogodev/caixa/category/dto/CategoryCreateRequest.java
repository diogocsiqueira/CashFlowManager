package com.diogodev.caixa.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank(message = "Nome da categoria é obrigatório")
        @Size(max = 60, message = "Nome da categoria deve ter no máximo 60 caracteres")
        String name
) {
}
package com.diogodev.caixa.transaction.dto;

import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionUpdateRequest(

        @NotBlank(message = "Nome da transação é obrigatório")
        @Size(max = 120, message = "Nome da transação deve ter no máximo 120 caracteres")
        String name,

        @NotNull(message = "Tipo é obrigatório")
        TransactionType type,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @NotNull(message = "Data é obrigatória")
        LocalDate date,

        Long categoryId,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description
) {
}

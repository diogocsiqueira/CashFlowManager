package com.diogodev.caixa.domain.dto;

import com.diogodev.caixa.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(

        @NotNull
        TransactionType type,

        @NotNull
        @Positive BigDecimal amount,

        @NotNull
        LocalDate date,

        @NotBlank
        String category,

        String description
) {
}

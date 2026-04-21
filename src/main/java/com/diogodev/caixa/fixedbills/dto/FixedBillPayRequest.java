package com.diogodev.caixa.fixedbills.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FixedBillPayRequest(
        @Positive(message = "Valor deve ser maior que zero")
        BigDecimal amount
) {
}

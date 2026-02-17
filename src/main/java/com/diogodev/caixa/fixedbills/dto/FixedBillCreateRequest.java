package com.diogodev.caixa.fixedbills.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record FixedBillCreateRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull @Min(1) @Max(31) Integer dueDay
) {}

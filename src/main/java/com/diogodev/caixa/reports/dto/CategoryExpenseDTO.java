package com.diogodev.caixa.reports.dto;

import java.math.BigDecimal;

public record CategoryExpenseDTO(
        String category,
        BigDecimal amount
) {}

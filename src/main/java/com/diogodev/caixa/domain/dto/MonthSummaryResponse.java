package com.diogodev.caixa.domain.dto;

import java.math.BigDecimal;

public record MonthSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}

package com.diogodev.caixa.transaction.dto;

import java.math.BigDecimal;

public record MonthSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}

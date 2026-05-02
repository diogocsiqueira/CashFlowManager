package com.diogodev.caixa.reports.dto;

import java.math.BigDecimal;

public record IncomeExpenseDTO(
        Integer year,
        Integer month,
        BigDecimal income,
        BigDecimal expense
) {}
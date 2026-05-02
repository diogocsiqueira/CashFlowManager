package com.diogodev.caixa.reports.dto;

import java.math.BigDecimal;

public record MonthlyBalanceDTO(
        Integer year,
        Integer month,
        BigDecimal balance
) {}
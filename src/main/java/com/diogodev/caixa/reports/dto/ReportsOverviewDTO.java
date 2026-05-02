package com.diogodev.caixa.reports.dto;


import java.math.BigDecimal;
import java.util.List;

public record ReportsOverviewDTO(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        BigDecimal pendingAmount,
        List<CategoryExpenseDTO> expensesByCategory,
        List<MonthlyBalanceDTO> monthlyBalance,
        List<IncomeExpenseDTO> incomeVsExpense
) {}
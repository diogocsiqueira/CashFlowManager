package com.diogodev.caixa.goal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalCreateRequest(
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal initialAmount,
        LocalDate deadlineDate
) {
}
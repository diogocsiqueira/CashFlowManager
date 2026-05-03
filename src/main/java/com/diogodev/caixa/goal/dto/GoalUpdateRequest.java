package com.diogodev.caixa.goal.dto;

import com.diogodev.caixa.goal.domain.enuns.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalUpdateRequest(
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal initialAmount,
        LocalDate deadlineDate,
        GoalStatus status
) {
}
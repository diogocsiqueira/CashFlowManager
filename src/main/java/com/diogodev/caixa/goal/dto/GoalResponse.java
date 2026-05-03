package com.diogodev.caixa.goal.dto;

import com.diogodev.caixa.goal.domain.enuns.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalResponse(
        Long id,
        String name,
        String description,
        BigDecimal targetAmount,
        BigDecimal initialAmount,
        BigDecimal currentAmount,
        BigDecimal remainingAmount,
        BigDecimal progressPercentage,
        LocalDate deadlineDate,
        GoalStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
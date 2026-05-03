package com.diogodev.caixa.goal.dto;

import com.diogodev.caixa.goal.domain.enuns.GoalContributionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalContributionResponse(
        Long id,
        Long goalId,
        GoalContributionType type,
        BigDecimal amount,
        String description,
        LocalDate contributionDate,
        LocalDateTime createdAt
) {
}
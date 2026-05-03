package com.diogodev.caixa.goal.dto;

import com.diogodev.caixa.goal.domain.enuns.GoalContributionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalContributionCreateRequest(
        GoalContributionType type,
        BigDecimal amount,
        String description,
        LocalDate contributionDate,

        Boolean createTransaction,
        Long transactionCategoryId
) {
}
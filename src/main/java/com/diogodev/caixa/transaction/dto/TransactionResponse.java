package com.diogodev.caixa.transaction.dto;

import com.diogodev.caixa.transaction.domain.enuns.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String name,
        TransactionType type,
        BigDecimal amount,
        LocalDate date,
        Long categoryId,
        String categoryName,
        String description
) {}

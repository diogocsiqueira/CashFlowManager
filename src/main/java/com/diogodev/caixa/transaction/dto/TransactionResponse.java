package com.diogodev.caixa.transaction.dto;

import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        LocalDate date,
        String category,
        String description
) {}

package com.diogodev.caixa.domain.dto;

import com.diogodev.caixa.domain.enums.TransactionType;
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

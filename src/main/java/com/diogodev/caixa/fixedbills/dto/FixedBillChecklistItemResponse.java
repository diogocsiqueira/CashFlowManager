package com.diogodev.caixa.fixedbills.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FixedBillChecklistItemResponse(
        Long fixedBillId,
        String name,
        BigDecimal amount,
        BigDecimal defaultAmount,
        Integer dueDay,
        Boolean paid,
        LocalDateTime paidAt
) {}

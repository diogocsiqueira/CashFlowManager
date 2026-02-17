package com.diogodev.caixa.fixedbills.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FixedBillChecklistItemResponse(
        Long fixedBillId,
        String name,
        BigDecimal amount,
        Integer dueDay,
        Boolean paid,
        LocalDateTime paidAt
) {}

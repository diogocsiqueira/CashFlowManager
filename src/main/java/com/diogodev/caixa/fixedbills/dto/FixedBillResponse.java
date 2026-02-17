package com.diogodev.caixa.fixedbills.dto;

import java.math.BigDecimal;

public record FixedBillResponse(
        Long id,
        String name,
        BigDecimal amount,
        Integer dueDay,
        Boolean active
) {}

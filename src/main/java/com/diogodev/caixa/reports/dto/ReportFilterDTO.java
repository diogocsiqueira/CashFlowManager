package com.diogodev.caixa.reports.dto;

import java.time.LocalDate;

public record ReportFilterDTO(
        LocalDate startDate,
        LocalDate endDate,
        Long categoryId,
        Boolean paid
) {}
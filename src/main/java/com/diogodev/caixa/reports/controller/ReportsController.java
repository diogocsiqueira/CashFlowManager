package com.diogodev.caixa.reports.controller;

import com.diogodev.caixa.reports.dto.ReportFilterDTO;
import com.diogodev.caixa.reports.dto.ReportsOverviewDTO;
import com.diogodev.caixa.reports.service.ReportsService;
import com.diogodev.caixa.shared.security.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final ReportsService service;

    public ReportsController(ReportsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ReportsOverviewDTO overview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId
    ) {

        Long userId = SecurityUtils.currentUserId();

        var filter = new ReportFilterDTO(
                startDate,
                endDate,
                categoryId,
                null
        );

        return service.getOverview(userId, filter);
    }
}
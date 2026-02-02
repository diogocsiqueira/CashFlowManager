package com.diogodev.caixa.controller;

import com.diogodev.caixa.domain.dto.FixedBillChecklistItemResponse;
import com.diogodev.caixa.service.FixedBillService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/months/{month}/fixed-bills")
public class FixedBillMonthController {

    private final FixedBillService fixedBillService;

    public FixedBillMonthController(FixedBillService fixedBillService) {
        this.fixedBillService = fixedBillService;
    }

    @GetMapping
    public List<FixedBillChecklistItemResponse> checklist(@PathVariable String month) {
        YearMonth ym = YearMonth.parse(month); // "2026-02"
        return fixedBillService.checklist(ym);
    }

    @PostMapping("/{billId}/pay")
    public FixedBillChecklistItemResponse pay(@PathVariable String month, @PathVariable Long billId) {
        YearMonth ym = YearMonth.parse(month);
        return fixedBillService.pay(ym, billId);
    }

    @PostMapping("/{billId}/unpay")
    public FixedBillChecklistItemResponse unpay(@PathVariable String month, @PathVariable Long billId) {
        YearMonth ym = YearMonth.parse(month);
        return fixedBillService.unpay(ym, billId);
    }
}

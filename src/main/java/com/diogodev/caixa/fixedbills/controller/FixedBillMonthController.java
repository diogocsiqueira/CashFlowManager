package com.diogodev.caixa.fixedbills.controller;

import com.diogodev.caixa.fixedbills.dto.FixedBillChecklistItemResponse;
import com.diogodev.caixa.fixedbills.dto.FixedBillPayRequest;
import com.diogodev.caixa.fixedbills.service.FixedBillService;
import jakarta.validation.Valid;
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
        YearMonth ym = YearMonth.parse(month);
        return fixedBillService.checklist(ym);
    }

    @PostMapping("/{billId}/pay")
    public FixedBillChecklistItemResponse pay(@PathVariable String month,
                                              @PathVariable Long billId,
                                              @Valid @RequestBody(required = false) FixedBillPayRequest request) {
        YearMonth ym = YearMonth.parse(month);
        return fixedBillService.pay(ym, billId, request);
    }

    @PostMapping("/{billId}/unpay")
    public FixedBillChecklistItemResponse unpay(@PathVariable String month, @PathVariable Long billId) {
        YearMonth ym = YearMonth.parse(month);
        return fixedBillService.unpay(ym, billId);
    }
}

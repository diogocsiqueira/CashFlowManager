package com.diogodev.caixa.controller;

import com.diogodev.caixa.domain.dto.MonthSummaryResponse;
import com.diogodev.caixa.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/summary")
public class SummaryController {

    private final TransactionService transactionService;

    public SummaryController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/balance")
    public MonthSummaryResponse summary(@RequestParam String month){
        YearMonth ym = YearMonth.parse(month);
        return transactionService.calculateBalance(ym);
    }
}

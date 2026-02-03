package com.diogodev.caixa.controller;

import com.diogodev.caixa.domain.dto.TransactionResponse;
import com.diogodev.caixa.domain.model.Transaction;
import com.diogodev.caixa.domain.dto.TransactionCreateRequest;
import com.diogodev.caixa.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionCreateRequest request){
        return transactionService.create(request);
    }


    @GetMapping("/transactions")
    public List<TransactionResponse> listByMonth(@RequestParam String month){
        YearMonth ym = YearMonth.parse(month);
        return transactionService.findByMonth(ym);
    }


}
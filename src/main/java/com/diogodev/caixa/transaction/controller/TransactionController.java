package com.diogodev.caixa.transaction.controller;

import com.diogodev.caixa.transaction.dto.TransactionResponse;
import com.diogodev.caixa.transaction.dto.TransactionCreateRequest;
import com.diogodev.caixa.transaction.dto.TransactionUpdateRequest;
import com.diogodev.caixa.transaction.service.TransactionService;
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

    @PutMapping("/transactions/{id}")
    public TransactionResponse update(@PathVariable Long id,
                                      @Valid @RequestBody TransactionUpdateRequest request) {
        return transactionService.update(id, request);
    }

    @DeleteMapping("/transactions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }

}
package com.diogodev.caixa.service;

import com.diogodev.caixa.domain.Transaction;
import com.diogodev.caixa.domain.dto.TransactionCreateRequest;
import com.diogodev.caixa.domain.enums.TransactionType;
import com.diogodev.caixa.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(TransactionCreateRequest request) {
        Transaction transaction = Transaction.builder()
                .type(request.type())
                .amount(request.amount())
                .date(request.date())
                .category(request.category())
                .description(request.description())
                .build();
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findByMonth(YearMonth month){

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return transactionRepository.findByDateBetween(start, end);
    }

    public BigDecimal calculateBalance(YearMonth month){

        List<Transaction> transactions = findByMonth(month);

        BigDecimal income = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return income.subtract(expense);

    }




}
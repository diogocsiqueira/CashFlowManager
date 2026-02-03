package com.diogodev.caixa.service;

import com.diogodev.caixa.domain.dto.MonthSummaryResponse;
import com.diogodev.caixa.domain.dto.TransactionCreateRequest;
import com.diogodev.caixa.domain.dto.TransactionResponse;
import com.diogodev.caixa.domain.enums.TransactionType;
import com.diogodev.caixa.domain.model.Transaction;
import com.diogodev.caixa.domain.model.User;
import com.diogodev.caixa.repository.TransactionRepository;
import com.diogodev.caixa.repository.UserRepository;
import com.diogodev.caixa.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    private Long uid() {
        return SecurityUtils.currentUserId();
    }

    private User currentUser() {
        return userRepository.findById(uid())
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe no banco"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getDate(),
                t.getCategory(),
                t.getDescription()
        );
    }

    // ✅ agora retorna DTO, não entidade
    public TransactionResponse create(TransactionCreateRequest request) {
        User user = currentUser();

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(request.type())
                .amount(request.amount())
                .date(request.date())
                .category(request.category())
                .description(request.description())
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    public List<TransactionResponse> findByMonth(YearMonth month) {
        Long userId = uid();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();


        return transactionRepository
                .findByUser_IdAndDateBetweenOrderByDateDesc(userId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ✅ calcula usando os DTOs (não precisa re-consultar o banco)
    public MonthSummaryResponse calculateBalance(YearMonth month) {
        List<TransactionResponse> transactions = findByMonth(month);

        BigDecimal income = transactions.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .map(TransactionResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = transactions.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .map(TransactionResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = income.subtract(expense);

        return new MonthSummaryResponse(income, expense, balance);
    }
}

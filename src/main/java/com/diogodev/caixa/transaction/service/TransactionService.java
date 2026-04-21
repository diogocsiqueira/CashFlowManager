package com.diogodev.caixa.transaction.service;

import com.diogodev.caixa.category.domain.model.Category;
import com.diogodev.caixa.category.service.CategoryService;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.shared.security.SecurityUtils;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.transaction.domain.model.Transaction;
import com.diogodev.caixa.transaction.dto.MonthSummaryResponse;
import com.diogodev.caixa.transaction.dto.TransactionCreateRequest;
import com.diogodev.caixa.transaction.dto.TransactionResponse;
import com.diogodev.caixa.transaction.dto.TransactionUpdateRequest;
import com.diogodev.caixa.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
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
                t.getName(),
                t.getType(),
                t.getAmount(),
                t.getDate(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getDescription()
        );
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        Transaction transaction = buildTransactionForCurrentUser(
                request.name(),
                request.type(),
                request.amount(),
                request.date(),
                request.categoryId(),
                request.description()
        );

        return toResponse(transactionRepository.save(transaction));
    }

    private Transaction buildTransactionForCurrentUser(String name,
                                                       TransactionType type,
                                                       BigDecimal amount,
                                                       LocalDate date,
                                                       Long categoryId,
                                                       String description) {
        User user = currentUser();
        Category category = categoryService.resolveCategoryForCurrentUser(categoryId);

        return Transaction.builder()
                .user(user)
                .name(normalizeName(name))
                .type(type)
                .amount(normalizeAmount(amount))
                .date(normalizeDate(date))
                .category(category)
                .description(normalizeDescription(description))
                .build();
    }

    private Transaction findOwnedTransaction(Long transactionId) {
        return transactionRepository.findByIdAndUser_Id(transactionId, uid())
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionUpdateRequest request) {
        Transaction transaction = findOwnedTransaction(id);
        Category category = categoryService.resolveCategoryForCurrentUser(request.categoryId());

        transaction.setName(normalizeName(request.name()));
        transaction.setType(request.type());
        transaction.setAmount(normalizeAmount(request.amount()));
        transaction.setDate(normalizeDate(request.date()));
        transaction.setCategory(category);
        transaction.setDescription(normalizeDescription(request.description()));

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(Long id) {
        Transaction transaction = findOwnedTransaction(id);
        transactionRepository.delete(transaction);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Nome da transação é obrigatório");
        }

        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }

        return amount;
    }

    private LocalDate normalizeDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }

        return date;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isBlank() ? null : normalized;
    }
}

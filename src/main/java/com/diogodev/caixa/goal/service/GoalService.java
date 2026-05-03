package com.diogodev.caixa.goal.service;

import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.goal.domain.enuns.GoalContributionType;
import com.diogodev.caixa.goal.domain.enuns.GoalStatus;
import com.diogodev.caixa.goal.domain.model.Goal;
import com.diogodev.caixa.goal.domain.model.GoalContribution;
import com.diogodev.caixa.goal.dto.*;
import com.diogodev.caixa.goal.repository.GoalContributionRepository;
import com.diogodev.caixa.goal.repository.GoalRepository;
import com.diogodev.caixa.shared.security.SecurityUtils;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.transaction.dto.TransactionCreateRequest;
import com.diogodev.caixa.transaction.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public GoalService(
            GoalRepository goalRepository,
            GoalContributionRepository contributionRepository,
            UserRepository userRepository,
            TransactionService transactionService
    ) {
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
    }

    private Long uid() {
        return SecurityUtils.currentUserId();
    }

    private User currentUser() {
        return userRepository.findById(uid())
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe no banco"));
    }

    @Transactional
    public GoalResponse create(GoalCreateRequest request) {
        User user = currentUser();

        Goal goal = Goal.builder()
                .user(user)
                .name(normalizeName(request.name()))
                .description(normalizeDescription(request.description()))
                .targetAmount(normalizeTargetAmount(request.targetAmount()))
                .initialAmount(normalizeInitialAmount(request.initialAmount()))
                .deadlineDate(request.deadlineDate())
                .status(GoalStatus.ACTIVE)
                .build();

        return toResponse(goalRepository.save(goal));
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> findAll() {
        return goalRepository.findByUser_IdOrderByCreatedAtDesc(uid())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse findById(Long id) {
        return toResponse(findOwnedGoal(id));
    }

    @Transactional
    public GoalResponse update(Long id, GoalUpdateRequest request) {
        Goal goal = findOwnedGoal(id);

        goal.setName(normalizeName(request.name()));
        goal.setDescription(normalizeDescription(request.description()));
        goal.setTargetAmount(normalizeTargetAmount(request.targetAmount()));
        goal.setInitialAmount(normalizeInitialAmount(request.initialAmount()));
        goal.setDeadlineDate(request.deadlineDate());
        goal.setStatus(request.status() == null ? GoalStatus.ACTIVE : request.status());

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void delete(Long id) {
        Goal goal = findOwnedGoal(id);
        goalRepository.delete(goal);
    }

    @Transactional
    public GoalContributionResponse createContribution(Long goalId, GoalContributionCreateRequest request) {
        Goal goal = findOwnedGoal(goalId);
        User user = currentUser();

        GoalContributionType type = normalizeContributionType(request.type());
        BigDecimal amount = normalizeAmount(request.amount());

        if (type == GoalContributionType.WITHDRAW) {
            BigDecimal currentAmount = calculateCurrentAmount(goal);

            if (currentAmount.subtract(amount).signum() < 0) {
                throw new IllegalArgumentException("Saque não pode deixar a meta negativa");
            }
        }

        GoalContribution contribution = GoalContribution.builder()
                .user(user)
                .goal(goal)
                .type(type)
                .amount(amount)
                .description(normalizeDescription(request.description()))
                .contributionDate(normalizeContributionDate(request.contributionDate()))
                .build();

        GoalContribution saved = contributionRepository.save(contribution);

        if (Boolean.TRUE.equals(request.createTransaction())) {
            createTransactionFromContribution(goal, request, type, amount);
        }

        return toContributionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GoalContributionResponse> findContributions(Long goalId) {
        findOwnedGoal(goalId);

        return contributionRepository
                .findByGoal_IdAndUser_IdOrderByContributionDateDesc(goalId, uid())
                .stream()
                .map(this::toContributionResponse)
                .toList();
    }

    private void createTransactionFromContribution(
            Goal goal,
            GoalContributionCreateRequest request,
            GoalContributionType contributionType,
            BigDecimal amount
    ) {
        if (request.transactionCategoryId() == null) {
            throw new IllegalArgumentException("Categoria da transação é obrigatória ao criar transação automática");
        }

        TransactionType transactionType = contributionType == GoalContributionType.DEPOSIT
                ? TransactionType.EXPENSE
                : TransactionType.INCOME;

        String transactionName = contributionType == GoalContributionType.DEPOSIT
                ? "Aporte para meta: " + goal.getName()
                : "Saque da meta: " + goal.getName();

        TransactionCreateRequest transactionRequest = new TransactionCreateRequest(
                transactionName,
                transactionType,
                amount,
                normalizeContributionDate(request.contributionDate()),
                request.transactionCategoryId(),
                normalizeDescription(request.description())
        );

        transactionService.create(transactionRequest);
    }

    private Goal findOwnedGoal(Long id) {
        return goalRepository.findByIdAndUser_Id(id, uid())
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
    }

    private GoalResponse toResponse(Goal goal) {
        BigDecimal currentAmount = calculateCurrentAmount(goal);
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentAmount);

        if (remainingAmount.signum() < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        BigDecimal progressPercentage = calculateProgressPercentage(currentAmount, goal.getTargetAmount());

        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getInitialAmount(),
                currentAmount,
                remainingAmount,
                progressPercentage,
                goal.getDeadlineDate(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private GoalContributionResponse toContributionResponse(GoalContribution contribution) {
        return new GoalContributionResponse(
                contribution.getId(),
                contribution.getGoal().getId(),
                contribution.getType(),
                contribution.getAmount(),
                contribution.getDescription(),
                contribution.getContributionDate(),
                contribution.getCreatedAt()
        );
    }

    private BigDecimal calculateCurrentAmount(Goal goal) {
        BigDecimal deposits = contributionRepository.sumByGoalAndType(
                goal.getId(),
                uid(),
                GoalContributionType.DEPOSIT
        );

        BigDecimal withdraws = contributionRepository.sumByGoalAndType(
                goal.getId(),
                uid(),
                GoalContributionType.WITHDRAW
        );

        return goal.getInitialAmount()
                .add(deposits)
                .subtract(withdraws);
    }

    private BigDecimal calculateProgressPercentage(BigDecimal currentAmount, BigDecimal targetAmount) {
        if (targetAmount == null || targetAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return currentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Nome da meta é obrigatório");
        }

        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private BigDecimal normalizeTargetAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Valor objetivo é obrigatório");
        }

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Valor objetivo deve ser maior que zero");
        }

        return amount;
    }

    private BigDecimal normalizeInitialAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Valor inicial não pode ser negativo");
        }

        return amount;
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

    private GoalContributionType normalizeContributionType(GoalContributionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Tipo do aporte é obrigatório");
        }

        return type;
    }

    private LocalDate normalizeContributionDate(LocalDate date) {
        return date == null ? LocalDate.now() : date;
    }
}
package com.diogodev.caixa.fixedbills.service;

import com.diogodev.caixa.category.domain.model.Category;
import com.diogodev.caixa.category.service.CategoryService;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.fixedbills.dto.FixedBillChecklistItemResponse;
import com.diogodev.caixa.fixedbills.dto.FixedBillCreateRequest;
import com.diogodev.caixa.fixedbills.dto.FixedBillPayRequest;
import com.diogodev.caixa.fixedbills.dto.FixedBillResponse;
import com.diogodev.caixa.fixedbills.model.FixedBill;
import com.diogodev.caixa.fixedbills.model.FixedBillPayment;
import com.diogodev.caixa.fixedbills.repository.FixedBillPaymentRepository;
import com.diogodev.caixa.fixedbills.repository.FixedBillRepository;
import com.diogodev.caixa.shared.security.SecurityUtils;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.transaction.dto.TransactionCreateRequest;
import com.diogodev.caixa.transaction.dto.TransactionResponse;
import com.diogodev.caixa.transaction.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FixedBillService {

    private final FixedBillRepository fixedBillRepository;
    private final FixedBillPaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public FixedBillService(FixedBillRepository fixedBillRepository,
                            FixedBillPaymentRepository paymentRepository,
                            UserRepository userRepository,
                            CategoryService categoryService,
                            TransactionService transactionService) {
        this.fixedBillRepository = fixedBillRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
    }

    private Long uid() {
        return SecurityUtils.currentUserId();
    }

    private User currentUser() {
        Long id = uid();
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe no banco"));
    }

    @Transactional(readOnly = true)
    public List<FixedBillResponse> listActive() {
        Long userId = uid();

        return fixedBillRepository
                .findByUser_IdAndActiveTrueOrderByDueDayAscNameAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FixedBillChecklistItemResponse> checklist(YearMonth month) {
        Long userId = uid();

        List<FixedBill> bills =
                fixedBillRepository.findByUser_IdAndActiveTrueOrderByDueDayAscNameAsc(userId);

        List<FixedBillPayment> payments =
                paymentRepository.findByFixedBill_User_IdAndMonth(userId, month);

        Map<Long, FixedBillPayment> payByBillId = payments.stream()
                .collect(Collectors.toMap(
                        p -> p.getFixedBill().getId(),
                        p -> p,
                        (a, b) -> a
                ));

        List<FixedBillChecklistItemResponse> items = new ArrayList<>(bills.size());

        for (FixedBill bill : bills) {
            FixedBillPayment p = payByBillId.get(bill.getId());
            boolean paid = p != null && Boolean.TRUE.equals(p.getPaid());
            BigDecimal amount = (p != null && p.getAmount() != null) ? p.getAmount() : bill.getAmount();
            LocalDateTime paidAt = (p != null) ? p.getPaidAt() : null;

            items.add(toChecklistItem(bill, amount, paid, paidAt));
        }

        return items;
    }

    @Transactional
    public FixedBillResponse create(FixedBillCreateRequest req) {
        User user = currentUser();
        Category category = categoryService.resolveCategoryForCurrentUser(req.categoryId());

        FixedBill bill = FixedBill.builder()
                .user(user)
                .name(normalizeName(req.name()))
                .amount(normalizeAmount(req.amount()))
                .dueDay(normalizeDueDay(req.dueDay()))
                .category(category)
                .active(true)
                .build();

        return toResponse(fixedBillRepository.save(bill));
    }

    @Transactional
    public FixedBillChecklistItemResponse pay(YearMonth month, Long fixedBillId, FixedBillPayRequest request) {
        Long userId = uid();

        FixedBill bill = fixedBillRepository.findByIdAndUser_Id(fixedBillId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada"));

        FixedBillPayment payment = paymentRepository
                .findByFixedBill_IdAndFixedBill_User_IdAndMonth(fixedBillId, userId, month)
                .orElse(null);

        if (payment != null && Boolean.TRUE.equals(payment.getPaid())) {
            return toChecklistItem(bill, payment.getAmount(), true, payment.getPaidAt());
        }

        BigDecimal paymentAmount = resolvePaymentAmount(bill, request);
        LocalDate paymentDate = resolvePaymentDate(month, bill.getDueDay());

        TransactionResponse transaction = transactionService.create(new TransactionCreateRequest(
                "Pagamento conta fixa: " + bill.getName(),
                TransactionType.EXPENSE,
                paymentAmount,
                paymentDate,
                bill.getCategory() != null ? bill.getCategory().getId() : null,
                "Conta fixa: " + bill.getName()
        ));

        if (payment == null) {
            payment = FixedBillPayment.builder()
                    .fixedBill(bill)
                    .month(month)
                    .paid(true)
                    .amount(paymentAmount)
                    .paidAt(LocalDateTime.now())
                    .transactionId(transaction.id())
                    .build();
        } else {
            payment.setPaid(true);
            payment.setAmount(paymentAmount);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(transaction.id());
        }

        paymentRepository.save(payment);

        return toChecklistItem(bill, paymentAmount, true, payment.getPaidAt());
    }

    @Transactional
    public FixedBillChecklistItemResponse unpay(YearMonth month, Long fixedBillId) {
        Long userId = uid();

        FixedBill bill = fixedBillRepository.findByIdAndUser_Id(fixedBillId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada"));

        FixedBillPayment payment = paymentRepository
                .findByFixedBill_IdAndFixedBill_User_IdAndMonth(fixedBillId, userId, month)
                .orElse(null);

        if (payment == null || !Boolean.TRUE.equals(payment.getPaid())) {
            return toChecklistItem(bill, bill.getAmount(), false, null);
        }

        Long txId = payment.getTransactionId();
        if (txId != null) {
            transactionService.delete(txId);
        }

        payment.setPaid(false);
        payment.setAmount(null);
        payment.setPaidAt(null);
        payment.setTransactionId(null);
        paymentRepository.save(payment);

        return toChecklistItem(bill, bill.getAmount(), false, null);
    }

    private BigDecimal resolvePaymentAmount(FixedBill bill, FixedBillPayRequest request) {
        if (request == null || request.amount() == null) {
            return bill.getAmount();
        }

        return normalizeAmount(request.amount());
    }

    private LocalDate resolvePaymentDate(YearMonth month, Integer dueDay) {
        int safeDay = Math.min(dueDay, month.lengthOfMonth());
        return month.atDay(safeDay);
    }

    private FixedBillChecklistItemResponse toChecklistItem(FixedBill bill,
                                                           BigDecimal amount,
                                                           boolean paid,
                                                           LocalDateTime paidAt) {
        return new FixedBillChecklistItemResponse(
                bill.getId(),
                bill.getName(),
                amount,
                bill.getAmount(),
                bill.getDueDay(),
                paid,
                paidAt
        );
    }

    private FixedBillResponse toResponse(FixedBill b) {
        return new FixedBillResponse(
                b.getId(),
                b.getName(),
                b.getAmount(),
                b.getDueDay(),
                b.getCategory() != null ? b.getCategory().getId() : null,
                b.getCategory() != null ? b.getCategory().getName() : null,
                b.getActive()
        );
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Nome da conta fixa é obrigatório");
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

    private Integer normalizeDueDay(Integer dueDay) {
        if (dueDay == null) {
            throw new IllegalArgumentException("Dia de vencimento é obrigatório");
        }
        if (dueDay < 1 || dueDay > 31) {
            throw new IllegalArgumentException("Dia de vencimento deve estar entre 1 e 31");
        }
        return dueDay;
    }
}

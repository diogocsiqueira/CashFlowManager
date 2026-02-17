package com.diogodev.caixa.fixedbills.service;

import com.diogodev.caixa.fixedbills.dto.FixedBillChecklistItemResponse;
import com.diogodev.caixa.fixedbills.dto.FixedBillCreateRequest;
import com.diogodev.caixa.fixedbills.dto.FixedBillResponse;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.fixedbills.model.FixedBill;
import com.diogodev.caixa.fixedbills.model.FixedBillPayment;
import com.diogodev.caixa.transaction.domain.model.Transaction;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.fixedbills.repository.FixedBillPaymentRepository;
import com.diogodev.caixa.fixedbills.repository.FixedBillRepository;
import com.diogodev.caixa.transaction.repository.TransactionRepository;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.shared.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public FixedBillService(
            FixedBillRepository fixedBillRepository,
            FixedBillPaymentRepository paymentRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository
    ) {
        this.fixedBillRepository = fixedBillRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // Auth helpers
    // =========================
    private Long uid() {
        return SecurityUtils.currentUserId();
    }

    private User currentUser() {
        Long id = uid();
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe no banco"));
    }

    // =========================
    // Queries
    // =========================
    public List<FixedBillResponse> listActive() {
        Long userId = uid();

        return fixedBillRepository
                .findByUser_IdAndActiveTrueOrderByDueDayAscNameAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Checklist do mês:
     * - bills ativos do usuário
     * - payments do mês do usuário
     * - retorna "pago/nao pago" por bill
     */
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
            LocalDateTime paidAt = (p != null) ? p.getPaidAt() : null;

            items.add(new FixedBillChecklistItemResponse(
                    bill.getId(),
                    bill.getName(),
                    bill.getAmount(),
                    bill.getDueDay(),
                    paid,
                    paidAt
            ));
        }

        return items;
    }

    // =========================
    // Commands
    // =========================
    @Transactional
    public FixedBillResponse create(FixedBillCreateRequest req) {
        User user = currentUser();

        FixedBill bill = FixedBill.builder()
                .user(user)
                .name(req.name().trim())
                .amount(req.amount())
                .dueDay(req.dueDay())
                .active(true)
                .build();

        return toResponse(fixedBillRepository.save(bill));
    }

    /**
     * Marcar como pago:
     * - garante bill pertence ao user
     * - cria/atualiza payment do mês
     * - cria Transaction EXPENSE do user e vincula pelo transactionId
     */
    @Transactional
    public FixedBillChecklistItemResponse pay(YearMonth month, Long fixedBillId) {
        Long userId = uid();
        User user = currentUser();

        FixedBill bill = fixedBillRepository.findByIdAndUser_Id(fixedBillId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada"));

        FixedBillPayment payment = paymentRepository
                .findByFixedBill_IdAndFixedBill_User_IdAndMonth(fixedBillId, userId, month)
                .orElse(null);

        if (payment != null && Boolean.TRUE.equals(payment.getPaid())) {
            return new FixedBillChecklistItemResponse(
                    bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                    true, payment.getPaidAt()
            );
        }

        // cria transação do usuário (obrigatório, Transaction.user é NOT NULL)
        Transaction tx = Transaction.builder()
                .user(user)
                .type(TransactionType.EXPENSE)
                .amount(bill.getAmount())
                .date(LocalDate.now())
                .category("CONTA_FIXA")
                .description("Conta fixa: " + bill.getName())
                .build();

        tx = transactionRepository.save(tx);

        if (payment == null) {
            payment = FixedBillPayment.builder()
                    .fixedBill(bill)
                    .month(month)
                    .paid(true)
                    .paidAt(LocalDateTime.now())
                    .transactionId(tx.getId())
                    .build();
        } else {
            payment.setPaid(true);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(tx.getId());
        }

        paymentRepository.save(payment);

        return new FixedBillChecklistItemResponse(
                bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                true, payment.getPaidAt()
        );
    }

    /**
     * Desmarcar pagamento:
     * - garante bill pertence ao user
     * - se tinha transactionId, apaga a Transaction do user (seguro)
     * - limpa payment
     */
    @Transactional
    public FixedBillChecklistItemResponse unpay(YearMonth month, Long fixedBillId) {
        Long userId = uid();

        FixedBill bill = fixedBillRepository.findByIdAndUser_Id(fixedBillId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada"));

        FixedBillPayment payment = paymentRepository
                .findByFixedBill_IdAndFixedBill_User_IdAndMonth(fixedBillId, userId, month)
                .orElse(null);

        if (payment == null || !Boolean.TRUE.equals(payment.getPaid())) {
            return new FixedBillChecklistItemResponse(
                    bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                    false, null
            );
        }

        Long txId = payment.getTransactionId();
        if (txId != null) {
            // segurança: só apaga se a tx for do usuário logado
            transactionRepository.deleteByIdAndUser_Id(txId, userId);
        }

        payment.setPaid(false);
        payment.setPaidAt(null);
        payment.setTransactionId(null);
        paymentRepository.save(payment);

        return new FixedBillChecklistItemResponse(
                bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                false, null
        );
    }

    // =========================
    // Mapper
    // =========================
    private FixedBillResponse toResponse(FixedBill b) {
        return new FixedBillResponse(
                b.getId(),
                b.getName(),
                b.getAmount(),
                b.getDueDay(),
                b.getActive()
        );
    }
}

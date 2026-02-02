package com.diogodev.caixa.service;

import com.diogodev.caixa.domain.dto.FixedBillChecklistItemResponse;
import com.diogodev.caixa.domain.dto.FixedBillCreateRequest;
import com.diogodev.caixa.domain.dto.FixedBillResponse;
import com.diogodev.caixa.domain.enums.TransactionType;
import com.diogodev.caixa.domain.model.FixedBill;
import com.diogodev.caixa.domain.model.FixedBillPayment;
import com.diogodev.caixa.domain.model.Transaction;
import com.diogodev.caixa.repository.FixedBillPaymentRepository;
import com.diogodev.caixa.repository.FixedBillRepository;
import com.diogodev.caixa.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FixedBillService {

    private final FixedBillRepository fixedBillRepository;
    private final FixedBillPaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    public FixedBillService(
            FixedBillRepository fixedBillRepository,
            FixedBillPaymentRepository paymentRepository,
            TransactionRepository transactionRepository
    ) {
        this.fixedBillRepository = fixedBillRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<FixedBillResponse> listActive() {
        return fixedBillRepository.findByActiveTrueOrderByDueDayAscNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FixedBillResponse create(FixedBillCreateRequest req) {
        FixedBill bill = FixedBill.builder()
                .name(req.name().trim())
                .amount(req.amount())
                .dueDay(req.dueDay())
                .active(true)
                .build();

        return toResponse(fixedBillRepository.save(bill));
    }

    // Checklist do mês: junta FixedBill (cadastro) com Payment (status do mês)
    public List<FixedBillChecklistItemResponse> checklist(YearMonth month) {

        List<FixedBill> bills = fixedBillRepository.findByActiveTrueOrderByDueDayAscNameAsc();
        List<FixedBillPayment> payments = paymentRepository.findByMonth(month);

        Map<Long, FixedBillPayment> payByBillId = payments.stream()
                .collect(Collectors.toMap(p -> p.getFixedBill().getId(), p -> p, (a, b) -> a));

        List<FixedBillChecklistItemResponse> items = new ArrayList<>();

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

    // Marcar como pago: cria Payment + cria Transaction EXPENSE vinculada
    @Transactional
    public FixedBillChecklistItemResponse pay(YearMonth month, Long fixedBillId) {

        FixedBill bill = fixedBillRepository.findById(fixedBillId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada: " + fixedBillId));

        FixedBillPayment payment = paymentRepository.findByFixedBill_IdAndMonth(fixedBillId, month)
                .orElse(null);

        if (payment != null && Boolean.TRUE.equals(payment.getPaid())) {
            return new FixedBillChecklistItemResponse(
                    bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                    true, payment.getPaidAt()
            );
        }

        // cria transação de gasto
        Transaction tx = Transaction.builder()
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

    // Desmarcar: marca como não pago + apaga a Transaction vinculada
    @Transactional
    public FixedBillChecklistItemResponse unpay(YearMonth month, Long fixedBillId) {

        FixedBill bill = fixedBillRepository.findById(fixedBillId)
                .orElseThrow(() -> new IllegalArgumentException("Conta fixa não encontrada: " + fixedBillId));

        FixedBillPayment payment = paymentRepository.findByFixedBill_IdAndMonth(fixedBillId, month)
                .orElse(null);

        if (payment == null || !Boolean.TRUE.equals(payment.getPaid())) {
            return new FixedBillChecklistItemResponse(
                    bill.getId(), bill.getName(), bill.getAmount(), bill.getDueDay(),
                    false, null
            );
        }

        Long txId = payment.getTransactionId();
        if (txId != null) {
            transactionRepository.deleteById(txId);
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

    private FixedBillResponse toResponse(FixedBill b) {
        return new FixedBillResponse(b.getId(), b.getName(), b.getAmount(), b.getDueDay(), b.getActive());
    }
}

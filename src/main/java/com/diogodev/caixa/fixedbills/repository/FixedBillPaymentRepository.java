package com.diogodev.caixa.fixedbills.repository;

import com.diogodev.caixa.fixedbills.model.FixedBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FixedBillPaymentRepository extends JpaRepository<FixedBillPayment, Long> {

    List<FixedBillPayment> findByFixedBill_User_IdAndMonth(Long userId, YearMonth month);

    Optional<FixedBillPayment> findByFixedBill_IdAndFixedBill_User_IdAndMonth(Long billId, Long userId, YearMonth month);
}


package com.diogodev.caixa.repository;

import com.diogodev.caixa.domain.model.FixedBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FixedBillPaymentRepository extends JpaRepository<FixedBillPayment, Long> {

    List<FixedBillPayment> findByMonth(YearMonth month);

    Optional<FixedBillPayment> findByFixedBill_IdAndMonth(Long fixedBillId, YearMonth month);
}

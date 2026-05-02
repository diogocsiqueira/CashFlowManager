package com.diogodev.caixa.fixedbills.repository;

import com.diogodev.caixa.fixedbills.model.FixedBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FixedBillRepository extends JpaRepository<FixedBill, Long> {

    List<FixedBill> findByUser_IdAndActiveTrueOrderByDueDayAscNameAsc(Long userId);

    Optional<FixedBill> findByIdAndUser_Id(Long id, Long userId);

    @Query("""
        SELECT COALESCE(SUM(b.amount), 0)
        FROM FixedBill b
        WHERE b.user.id = :userId
          AND b.active = true
          AND (:categoryId IS NULL OR b.category.id = :categoryId)
          AND NOT EXISTS (
              SELECT 1
              FROM FixedBillPayment p
              WHERE p.fixedBill = b
                AND p.month BETWEEN :startMonth AND :endMonth
                AND p.paid = true
          )
    """)
    BigDecimal sumPendingActiveBills(
            Long userId,
            YearMonth startMonth,
            YearMonth endMonth,
            Long categoryId
    );
}
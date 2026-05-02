package com.diogodev.caixa.transaction.repository;

import com.diogodev.caixa.reports.dto.CategoryExpenseDTO;
import com.diogodev.caixa.reports.dto.IncomeExpenseDTO;
import com.diogodev.caixa.reports.dto.MonthlyBalanceDTO;
import com.diogodev.caixa.transaction.domain.enuns.TransactionType;
import com.diogodev.caixa.transaction.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser_IdAndDateBetweenOrderByDateDesc(
            Long userId,
            LocalDate start,
            LocalDate end
    );

    Optional<Transaction> findByIdAndUser_Id(Long id, Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndCategory_Id(Long userId, Long categoryId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND t.date BETWEEN :startDate AND :endDate
          AND (:categoryId IS NULL OR t.category.id = :categoryId)
    """)
    BigDecimal sumByType(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId
    );

    @Query("""
        SELECT new com.diogodev.caixa.reports.dto.CategoryExpenseDTO(
            t.category.name,
            COALESCE(SUM(t.amount), 0)
        )
        FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :expenseType
          AND t.date BETWEEN :startDate AND :endDate
          AND (:categoryId IS NULL OR t.category.id = :categoryId)
        GROUP BY t.category.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<CategoryExpenseDTO> expensesByCategory(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            TransactionType expenseType
    );

    @Query("""
    SELECT new com.diogodev.caixa.reports.dto.MonthlyBalanceDTO(
        YEAR(t.date),
        MONTH(t.date),
        COALESCE(SUM(CASE WHEN t.type = :incomeType THEN t.amount ELSE 0 END), 0)
        -
        COALESCE(SUM(CASE WHEN t.type = :expenseType THEN t.amount ELSE 0 END), 0)
    )
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.date BETWEEN :startDate AND :endDate
      AND (:categoryId IS NULL OR t.category.id = :categoryId)
    GROUP BY YEAR(t.date), MONTH(t.date)
    ORDER BY YEAR(t.date), MONTH(t.date)
""")
    List<MonthlyBalanceDTO> monthlyBalance(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            TransactionType incomeType,
            TransactionType expenseType
    );

    @Query("""
    SELECT new com.diogodev.caixa.reports.dto.IncomeExpenseDTO(
        YEAR(t.date),
        MONTH(t.date),
        COALESCE(SUM(CASE WHEN t.type = :incomeType THEN t.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.type = :expenseType THEN t.amount ELSE 0 END), 0)
    )
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.date BETWEEN :startDate AND :endDate
      AND (:categoryId IS NULL OR t.category.id = :categoryId)
    GROUP BY YEAR(t.date), MONTH(t.date)
    ORDER BY YEAR(t.date), MONTH(t.date)
""")
    List<IncomeExpenseDTO> incomeVsExpense(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Long categoryId,
            TransactionType incomeType,
            TransactionType expenseType
    );
}
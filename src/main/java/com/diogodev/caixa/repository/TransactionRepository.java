package com.diogodev.caixa.repository;

import com.diogodev.caixa.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser_IdAndDateBetweenOrderByDateDesc(Long userId, LocalDate start, LocalDate end);

    void deleteByIdAndUser_Id(Long id, Long userId);
}


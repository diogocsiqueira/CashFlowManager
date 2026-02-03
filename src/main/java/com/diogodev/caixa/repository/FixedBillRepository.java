package com.diogodev.caixa.repository;

import com.diogodev.caixa.domain.model.FixedBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FixedBillRepository extends JpaRepository<FixedBill, Long> {

    List<FixedBill> findByUser_IdAndActiveTrueOrderByDueDayAscNameAsc(Long userId);

    Optional<FixedBill> findByIdAndUser_Id(Long id, Long userId);
}


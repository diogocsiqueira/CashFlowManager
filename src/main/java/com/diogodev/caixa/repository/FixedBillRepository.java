package com.diogodev.caixa.repository;

import com.diogodev.caixa.domain.model.FixedBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedBillRepository extends JpaRepository<FixedBill, Long> {

    List<FixedBill> findByActiveTrueOrderByDueDayAscNameAsc();
}

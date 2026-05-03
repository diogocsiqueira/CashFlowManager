package com.diogodev.caixa.goal.repository;

import com.diogodev.caixa.goal.domain.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUser_IdOrderByCreatedAtDesc(Long userId);

    Optional<Goal> findByIdAndUser_Id(Long id, Long userId);
}
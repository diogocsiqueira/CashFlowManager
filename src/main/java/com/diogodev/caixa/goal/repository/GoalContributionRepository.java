package com.diogodev.caixa.goal.repository;

import com.diogodev.caixa.goal.domain.enuns.GoalContributionType;
import com.diogodev.caixa.goal.domain.model.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {

    List<GoalContribution> findByGoal_IdAndUser_IdOrderByContributionDateDesc(
            Long goalId,
            Long userId
    );

    @Query("""
            select coalesce(sum(c.amount), 0)
            from GoalContribution c
            where c.goal.id = :goalId
              and c.user.id = :userId
              and c.type = :type
            """)
    BigDecimal sumByGoalAndType(Long goalId, Long userId, GoalContributionType type);
}
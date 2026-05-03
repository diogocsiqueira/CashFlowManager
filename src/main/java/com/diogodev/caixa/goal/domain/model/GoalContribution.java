package com.diogodev.caixa.goal.domain.model;

import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.goal.domain.enuns.GoalContributionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goal_contributions")
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalContributionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate contributionDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.contributionDate == null) {
            this.contributionDate = LocalDate.now();
        }
    }
}
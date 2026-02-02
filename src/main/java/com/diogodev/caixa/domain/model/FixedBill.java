package com.diogodev.caixa.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fixed_bills")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer dueDay; // 1..31

    @Column(nullable = false)
    private Boolean active;
}

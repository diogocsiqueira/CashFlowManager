package com.diogodev.caixa.fixedbills.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(
        name = "fixed_bill_payments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fixed_bill_id", "reference_month"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedBillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fixed_bill_id", nullable = false)
    private FixedBill fixedBill;

    @Column(name = "reference_month", nullable = false, length = 7)
    private YearMonth month;

    @Column(nullable = false)
    private Boolean paid;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDateTime paidAt;

    private Long transactionId;
}

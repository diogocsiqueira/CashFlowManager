package com.diogodev.caixa.fixedbills.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(
        name = "fixed_bill_payments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fixed_bill_id", "month"})
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedBillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conta fixa (cadastro)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fixed_bill_id", nullable = false)
    private FixedBill fixedBill;

    // Mês de referência (salvo como "YYYY-MM" via converter)
    @Column(name = "reference_month", nullable = false, length = 7)
    private YearMonth month;

    @Column(nullable = false)
    private Boolean paid;

    private LocalDateTime paidAt;

    // Id da Transaction EXPENSE que foi gerada ao marcar como paga
    private Long transactionId;
}

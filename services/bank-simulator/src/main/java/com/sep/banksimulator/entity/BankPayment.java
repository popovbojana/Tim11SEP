package com.sep.banksimulator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank_payments")
public class BankPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "psp_payment_id", nullable = false)
    private Long pspPaymentId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String stan;

    @Column(name = "psp_timestamp", nullable = false)
    private Instant pspTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BankPaymentStatus status;

    @Column
    private String globalTransactionId;

    @Column
    private Instant acquirerTimestamp;

    @Column(nullable = false)
    private Instant createdAt;

}

package com.sep.banksimulator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(name = "merchant_order_id", nullable = false)
    private String merchantOrderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

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

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column
    private String receiverName;

    @Column
    private String receiverAccount;

}

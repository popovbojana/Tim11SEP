package com.sep.psp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merchantOrderId;

    @Column(nullable = false)
    private String merchantKey;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 2048)
    private String successUrl;

    @Column(nullable = false, length = 2048)
    private String failedUrl;

    @Column(nullable = false, length = 2048)
    private String errorUrl;

    @Column(name = "bank_payment_id")
    private Long bankPaymentId;

    @Column(length = 2048)
    private String bankRedirectUrl;

    @Column(nullable = false)
    private String stan;

    @Column(nullable = false)
    private Instant pspTimestamp;
}

package com.sep.psp.entity;

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
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merchantOrderId;

    @Column(nullable = false)
    private String merchantKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

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

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "external_redirect_url", length = 2048)
    private String externalRedirectUrl;

    @Column(nullable = false)
    private String stan;

    @Column(nullable = false)
    private Instant pspTimestamp;

    @Column(length = 64)
    private String globalTransactionId;

    private Instant acquirerTimestamp;

    private String paymentMethod;

}
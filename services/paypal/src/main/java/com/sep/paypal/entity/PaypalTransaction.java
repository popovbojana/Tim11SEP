package com.sep.paypal.entity;

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
@Table(name = "paypal_transactions")
public class PaypalTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pspPaymentId;

    @Column(nullable = false, unique = true)
    private String paypalOrderId;

    @Column(nullable = false)
    private String status;

    @Column
    private String merchantOrderId;

    @Column
    private String merchantKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column
    private String currency;

    @Column
    private Instant createdAt;

}
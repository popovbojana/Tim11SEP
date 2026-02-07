package com.sep.crypto.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long pspPaymentId;

    @Column
    private String coingateOrderId;

    @Column
    private String status;

    @Column
    private String merchantOrderId;

    @Column
    private String merchantKey;

    @Column
    private String currency;

    @Column
    private Double amount;

    @Column
    private Instant createdAt;

}
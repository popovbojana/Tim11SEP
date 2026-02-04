package com.sep.psp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "merchants")
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String merchantKey;

    @Column(nullable = false)
    private String merchantPassword;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String successUrl;

    @Column(nullable = false)
    private String failedUrl;

    @Column(nullable = false)
    private String errorUrl;

    @Column
    private String webhookUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "merchant_payment_methods",
            joinColumns = @JoinColumn(name = "merchant_id"),
            inverseJoinColumns = @JoinColumn(name = "payment_method_id")
    )
    @Builder.Default
    private Set<PaymentMethod> activeMethods = new HashSet<>();
}
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "merchant_payment_methods",
            joinColumns = @JoinColumn(name = "merchant_id"),
            inverseJoinColumns = @JoinColumn(name = "payment_method_id")
    )
    @Builder.Default
    private Set<PaymentMethod> activeMethods = new HashSet<>();
}
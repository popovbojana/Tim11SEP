package com.sep.banksimulator.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Double balance;

    @Column(nullable = false)
    private String currency;
}
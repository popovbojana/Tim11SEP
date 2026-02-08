package com.sep.banksimulator.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_cards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pan;
    private String expiryDate;

    @OneToOne
    @JoinColumn(name = "account_id")
    private BankAccount account;
}
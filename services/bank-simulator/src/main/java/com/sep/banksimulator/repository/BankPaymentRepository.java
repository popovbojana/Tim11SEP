package com.sep.banksimulator.repository;

import com.sep.banksimulator.entity.BankPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankPaymentRepository extends JpaRepository<BankPayment, Long> {
}

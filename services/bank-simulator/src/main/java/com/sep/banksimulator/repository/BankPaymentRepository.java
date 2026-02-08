package com.sep.banksimulator.repository;

import com.sep.banksimulator.entity.BankPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankPaymentRepository extends JpaRepository<BankPayment, Long> {

}

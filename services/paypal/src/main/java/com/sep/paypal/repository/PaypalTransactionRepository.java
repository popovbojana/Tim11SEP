package com.sep.paypal.repository;

import com.sep.paypal.entity.PaypalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaypalTransactionRepository extends JpaRepository<PaypalTransaction, Long> {

    Optional<PaypalTransaction> findByPaypalOrderId(String paypalOrderId);

}

package com.sep.crypto.repository;

import com.sep.crypto.entity.CryptoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {

    Optional<CryptoTransaction> findByCoingateOrderId(String coingateOrderId);

}
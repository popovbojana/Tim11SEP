package com.sep.banksimulator.repository;

import com.sep.banksimulator.entity.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {
    Optional<BankCard> findByPan(String pan);
}
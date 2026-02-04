package com.sep.psp.repository;

import com.sep.psp.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByName(String name);

    boolean existsByName(String name);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM merchant_payment_methods WHERE payment_method_id = :methodId", nativeQuery = true)
    void deleteRelationFromJoinTable(@Param("methodId") Long methodId);
}

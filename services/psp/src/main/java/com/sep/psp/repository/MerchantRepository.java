package com.sep.psp.repository;

import com.sep.psp.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByMerchantKey(String merchantKey);

    boolean existsByMerchantKey(String merchantKey);

}

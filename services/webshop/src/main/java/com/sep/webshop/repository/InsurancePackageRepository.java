package com.sep.webshop.repository;

import com.sep.webshop.entity.InsurancePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePackageRepository extends JpaRepository<InsurancePackage, Long> {

    List<InsurancePackage> findAllByActiveTrue();

    Optional<InsurancePackage> findByName(String name);

}

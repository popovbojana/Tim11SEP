package com.sep.webshop.repository;

import com.sep.webshop.entity.AdditionalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdditionalServiceRepository extends JpaRepository<AdditionalService, Long> {

    List<AdditionalService> findAllByActiveTrue();

    Optional<AdditionalService> findByName(String name);

}

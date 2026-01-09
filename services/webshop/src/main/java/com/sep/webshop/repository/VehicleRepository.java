package com.sep.webshop.repository;

import com.sep.webshop.entity.Vehicle;
import com.sep.webshop.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findAllByActiveTrue();

    List<Vehicle> findAllByActiveTrueAndType(VehicleType type);

}

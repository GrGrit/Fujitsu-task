package com.fujitsu.task.repository;

import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

/**
 * Data access for {@link RegionalBaseFee} rules.
 */
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {

    /**
     * Finds the base fee for a city and vehicle type that was in force at the given moment.
     */
    @Query("""
            select f from RegionalBaseFee f
            where f.city = :city and f.vehicleType = :vehicleType
              and f.validFrom <= :moment and (f.validTo is null or f.validTo > :moment)
            """)
    Optional<RegionalBaseFee> findValidAt(City city, VehicleType vehicleType, Instant moment);
}

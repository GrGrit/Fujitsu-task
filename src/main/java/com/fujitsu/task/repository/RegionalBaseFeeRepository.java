package com.fujitsu.task.repository;

import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
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

    /**
     * @return all rules currently in force
     */
    List<RegionalBaseFee> findAllByValidToIsNull();

    /**
     * @param id rule id
     * @return the rule if it exists and is currently in force
     */
    Optional<RegionalBaseFee> findByIdAndValidToIsNull(Long id);

    /**
     * @param city        delivery city
     * @param vehicleType courier vehicle type
     * @return true if a rule for the combination is currently in force
     */
    boolean existsByCityAndVehicleTypeAndValidToIsNull(City city, VehicleType vehicleType);
}

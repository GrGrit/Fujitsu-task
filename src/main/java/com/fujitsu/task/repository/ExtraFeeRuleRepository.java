package com.fujitsu.task.repository;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Data access for {@link ExtraFeeRule} rules.
 */
public interface ExtraFeeRuleRepository extends JpaRepository<ExtraFeeRule, Long> {

    /**
     * Finds all extra fee rules of a vehicle type that were in force at the given moment.
     */
    @Query("""
            select r from ExtraFeeRule r
            where r.vehicleType = :vehicleType
              and r.validFrom <= :moment and (r.validTo is null or r.validTo > :moment)
            """)
    List<ExtraFeeRule> findAllValidAt(VehicleType vehicleType, Instant moment);

    /**
     * @return all rules currently in force
     */
    List<ExtraFeeRule> findAllByValidToIsNull();

    /**
     * @return the rule if it exists and is currently in force
     */
    Optional<ExtraFeeRule> findByIdAndValidToIsNull(Long id);
}

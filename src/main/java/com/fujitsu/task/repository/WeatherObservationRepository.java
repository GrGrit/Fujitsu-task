package com.fujitsu.task.repository;

import com.fujitsu.task.entity.WeatherObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

/**
 * Data access for {@link WeatherObservation} records.
 */
public interface WeatherObservationRepository extends JpaRepository<WeatherObservation, Long> {

    /**
     * Finds the most recent observation of a station made at or before the given moment.
     */
    @Query("""
            select o from WeatherObservation o
            where o.stationName = :stationName and o.observationTimestamp <= :moment
            order by o.observationTimestamp desc, o.id desc
            limit 1
            """)
    Optional<WeatherObservation> findLatestAt(String stationName, Instant moment);
}

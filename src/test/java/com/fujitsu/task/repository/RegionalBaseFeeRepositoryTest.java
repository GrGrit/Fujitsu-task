package com.fujitsu.task.repository;

import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RegionalBaseFeeRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");

    @Autowired
    private RegionalBaseFeeRepository repository;

    @Test
    void initialDataContainsAllCityAndVehicleCombinations() {
        assertThat(repository.count()).isEqualTo(City.values().length * VehicleType.values().length);
    }

    @Test
    void findsInitialBaseFee() {
        Optional<RegionalBaseFee> fee = repository.findValidAt(City.TARTU, VehicleType.BIKE, NOW);

        assertThat(fee).get().extracting(RegionalBaseFee::getFee).isEqualTo(new BigDecimal("2.50"));
    }

    @Test
    void findsVersionValidAtGivenMoment() {
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        RegionalBaseFee current = repository.findValidAt(City.TARTU, VehicleType.BIKE, NOW).orElseThrow();
        current.setValidTo(changedAt);
        repository.save(RegionalBaseFee.builder()
                .city(City.TARTU)
                .vehicleType(VehicleType.BIKE)
                .fee(new BigDecimal("2.80"))
                .validFrom(changedAt)
                .build());

        assertThat(repository.findValidAt(City.TARTU, VehicleType.BIKE, NOW))
                .get().extracting(RegionalBaseFee::getFee).isEqualTo(new BigDecimal("2.80"));
        assertThat(repository.findValidAt(City.TARTU, VehicleType.BIKE, changedAt.minusSeconds(1)))
                .get().extracting(RegionalBaseFee::getFee).isEqualTo(new BigDecimal("2.50"));
    }

    @Test
    void returnsEmptyBeforeAnyRuleWasValid() {
        Optional<RegionalBaseFee> fee = repository.findValidAt(City.TARTU, VehicleType.BIKE,
                Instant.parse("1999-12-31T23:59:59Z"));

        assertThat(fee).isEmpty();
    }
}

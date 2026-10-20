package com.fujitsu.task.repository;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExtraFeeRuleRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");

    @Autowired
    private ExtraFeeRuleRepository repository;

    @Test
    void carHasNoExtraFeeRules() {
        assertThat(repository.findAllValidAt(VehicleType.CAR, NOW)).isEmpty();
    }

    @Test
    void scooterHasTemperatureAndPhenomenonRulesOnly() {
        List<ExtraFeeRule> rules = repository.findAllValidAt(VehicleType.SCOOTER, NOW);

        assertThat(rules)
                .extracting(ExtraFeeRule::getConditionType)
                .containsOnly(WeatherConditionType.AIR_TEMPERATURE, WeatherConditionType.WEATHER_PHENOMENON);
    }

    @Test
    void bikeHasRulesForAllConditions() {
        List<ExtraFeeRule> rules = repository.findAllValidAt(VehicleType.BIKE, NOW);

        assertThat(rules)
                .extracting(ExtraFeeRule::getConditionType)
                .contains(WeatherConditionType.AIR_TEMPERATURE,
                        WeatherConditionType.WIND_SPEED,
                        WeatherConditionType.WEATHER_PHENOMENON);
    }

    @Test
    void excludesClosedRules() {
        Instant closedAt = Instant.parse("2026-01-01T00:00:00Z");
        List<ExtraFeeRule> windRules = repository.findAllValidAt(VehicleType.BIKE, NOW).stream()
                .filter(rule -> rule.getConditionType() == WeatherConditionType.WIND_SPEED)
                .toList();
        windRules.forEach(rule -> rule.setValidTo(closedAt));
        repository.saveAll(windRules);

        assertThat(repository.findAllValidAt(VehicleType.BIKE, NOW))
                .extracting(ExtraFeeRule::getConditionType)
                .doesNotContain(WeatherConditionType.WIND_SPEED);
        assertThat(repository.findAllValidAt(VehicleType.BIKE, closedAt.minusSeconds(1)))
                .extracting(ExtraFeeRule::getConditionType)
                .contains(WeatherConditionType.WIND_SPEED);
    }
}

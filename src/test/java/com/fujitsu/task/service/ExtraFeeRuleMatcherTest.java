package com.fujitsu.task.service;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ExtraFeeRuleMatcherTest {

    private final ExtraFeeRuleMatcher matcher = new ExtraFeeRuleMatcher();

    @ParameterizedTest(name = "{0} °C in [-10, 0] -> {1}")
    @CsvSource({"-10.1, false", "-10.0, true", "-5.0, true", "0.0, true", "0.1, false"})
    void matchesInclusiveRange(double temperature, boolean expected) {
        ExtraFeeRule rule = rangeRule(WeatherConditionType.AIR_TEMPERATURE, "-10", true, "0", true);

        assertThat(matcher.matches(rule, observation(temperature, 0.0, null))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} °C < -10 -> {1}")
    @CsvSource({"-10.1, true", "-10.0, false", "-30.0, true"})
    void matchesExclusiveUpperBoundWithoutLowerBound(double temperature, boolean expected) {
        ExtraFeeRule rule = rangeRule(WeatherConditionType.AIR_TEMPERATURE, null, false, "-10", false);

        assertThat(matcher.matches(rule, observation(temperature, 0.0, null))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0} m/s > 20 -> {1}")
    @CsvSource({"20.0, false", "20.1, true", "35.0, true"})
    void matchesExclusiveLowerBoundWithoutUpperBound(double windSpeed, boolean expected) {
        ExtraFeeRule rule = rangeRule(WeatherConditionType.WIND_SPEED, "20", false, null, false);

        assertThat(matcher.matches(rule, observation(0.0, windSpeed, null))).isEqualTo(expected);
    }

    @Test
    void doesNotMatchMissingMeasurement() {
        ExtraFeeRule rule = rangeRule(WeatherConditionType.WIND_SPEED, "10", true, "20", true);

        assertThat(matcher.matches(rule, observation(0.0, null, null))).isFalse();
    }

    @ParameterizedTest(name = "\"{0}\" contains \"{1}\" -> {2}")
    @CsvSource({
            "Light snow shower, snow, true",
            "Light snow shower, shower, true",
            "Moderate SLEET, sleet, true",
            "Thunderstorm, thunder, true",
            "Clear, rain, false"
    })
    void matchesPhenomenonByKeywordIgnoringCase(String phenomenon, String keyword, boolean expected) {
        ExtraFeeRule rule = phenomenonRule(keyword);

        assertThat(matcher.matches(rule, observation(0.0, 0.0, phenomenon))).isEqualTo(expected);
    }

    @Test
    void doesNotMatchMissingPhenomenon() {
        assertThat(matcher.matches(phenomenonRule("rain"), observation(0.0, 0.0, null))).isFalse();
    }

    private static ExtraFeeRule rangeRule(WeatherConditionType type, String min, boolean minInclusive,
                                          String max, boolean maxInclusive) {
        return ExtraFeeRule.builder()
                .conditionType(type)
                .vehicleType(VehicleType.BIKE)
                .minValue(min == null ? null : new BigDecimal(min))
                .minInclusive(minInclusive)
                .maxValue(max == null ? null : new BigDecimal(max))
                .maxInclusive(maxInclusive)
                .fee(new BigDecimal("0.50"))
                .validFrom(Instant.EPOCH)
                .build();
    }

    private static ExtraFeeRule phenomenonRule(String keyword) {
        return ExtraFeeRule.builder()
                .conditionType(WeatherConditionType.WEATHER_PHENOMENON)
                .vehicleType(VehicleType.BIKE)
                .phenomenonKeyword(keyword)
                .fee(new BigDecimal("0.50"))
                .validFrom(Instant.EPOCH)
                .build();
    }

    private static WeatherObservation observation(Double temperature, Double windSpeed, String phenomenon) {
        return new WeatherObservation("Tartu-Tõravere", "26242", temperature, windSpeed, phenomenon, Instant.EPOCH);
    }
}

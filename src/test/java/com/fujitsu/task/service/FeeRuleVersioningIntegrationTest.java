package com.fujitsu.task.service;

import com.fujitsu.task.dto.ExtraFeeRuleRequest;
import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.DuplicateFeeRuleException;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.exception.InvalidFeeRuleException;
import com.fujitsu.task.mapper.ExtraFeeRuleMapper;
import com.fujitsu.task.mapper.RegionalBaseFeeMapper;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import com.fujitsu.task.repository.WeatherObservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that rule changes made through the CRUD services keep history, so fees can still be calculated
 * with the rules that applied at an earlier point in time.
 */
@DataJpaTest
@Import({RegionalBaseFeeService.class, RegionalBaseFeeMapper.class,
        ExtraFeeRuleService.class, ExtraFeeRuleMapper.class,
        DeliveryFeeService.class, ExtraFeeRuleMatcher.class,
        FeeRuleVersioningIntegrationTest.FixedClockConfig.class})
class FeeRuleVersioningIntegrationTest {

    private static final Instant CHANGED_AT = Instant.parse("2026-01-10T12:00:00Z");
    private static final Instant BEFORE_CHANGE = CHANGED_AT.minusSeconds(3600);
    private static final Instant AFTER_CHANGE = CHANGED_AT.plusSeconds(3600);

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(CHANGED_AT, ZoneOffset.UTC);
        }
    }

    @Autowired
    private RegionalBaseFeeService regionalBaseFeeService;

    @Autowired
    private ExtraFeeRuleService extraFeeRuleService;

    @Autowired
    private DeliveryFeeService deliveryFeeService;

    @Autowired
    private WeatherObservationRepository weatherObservationRepository;

    @Test
    void updatedBaseFeeAppliesOnlyFromChangeOnwards() {
        RegionalBaseFeeResponse current = findActiveBaseFee(City.TARTU, VehicleType.CAR);

        RegionalBaseFeeResponse updated = regionalBaseFeeService.update(current.id(),
                new RegionalBaseFeeRequest(City.TARTU, VehicleType.CAR, new BigDecimal("5.00")));

        assertThat(updated.id()).isNotEqualTo(current.id());
        assertThat(regionalBaseFeeService.findById(current.id()).validTo()).isEqualTo(CHANGED_AT);
        assertThat(deliveryFeeService.calculateFee(City.TARTU, VehicleType.CAR, BEFORE_CHANGE))
                .isEqualByComparingTo("3.50");
        assertThat(deliveryFeeService.calculateFee(City.TARTU, VehicleType.CAR, AFTER_CHANGE))
                .isEqualByComparingTo("5.00");
    }

    @Test
    void deletedBaseFeeIsNoLongerActiveButStillUsedForPastCalculations() {
        RegionalBaseFeeResponse current = findActiveBaseFee(City.PARNU, VehicleType.CAR);

        regionalBaseFeeService.delete(current.id());

        assertThat(regionalBaseFeeService.findAllActive()).noneMatch(fee -> fee.id().equals(current.id()));
        assertThat(deliveryFeeService.calculateFee(City.PARNU, VehicleType.CAR, BEFORE_CHANGE))
                .isEqualByComparingTo("3.00");
        assertThatThrownBy(() -> deliveryFeeService.calculateFee(City.PARNU, VehicleType.CAR, AFTER_CHANGE))
                .isInstanceOf(FeeRuleNotFoundException.class);
    }

    @Test
    void cannotDeleteSameRuleTwice() {
        RegionalBaseFeeResponse current = findActiveBaseFee(City.PARNU, VehicleType.BIKE);
        regionalBaseFeeService.delete(current.id());

        assertThatThrownBy(() -> regionalBaseFeeService.delete(current.id()))
                .isInstanceOf(FeeRuleNotFoundException.class);
    }

    @Test
    void rejectsSecondActiveBaseFeeForSameCombination() {
        assertThatThrownBy(() -> regionalBaseFeeService.create(
                new RegionalBaseFeeRequest(City.TALLINN, VehicleType.CAR, new BigDecimal("9.00"))))
                .isInstanceOf(DuplicateFeeRuleException.class);
    }

    @Test
    void newExtraFeeRuleAppliesOnlyFromCreationOnwards() {
        weatherObservationRepository.save(new WeatherObservation(
                City.TARTU.getStationName(), "26242", 5.0, 3.0, "Mist", BEFORE_CHANGE.minusSeconds(60)));

        ExtraFeeRuleResponse created = extraFeeRuleService.create(new ExtraFeeRuleRequest(
                WeatherConditionType.WEATHER_PHENOMENON, VehicleType.CAR,
                null, false, null, false, "mist", new BigDecimal("0.30"), false));

        assertThat(created.validFrom()).isEqualTo(CHANGED_AT);
        assertThat(deliveryFeeService.calculateFee(City.TARTU, VehicleType.CAR, BEFORE_CHANGE))
                .isEqualByComparingTo("3.50");
        assertThat(deliveryFeeService.calculateFee(City.TARTU, VehicleType.CAR, AFTER_CHANGE))
                .isEqualByComparingTo("3.80");
    }

    @Test
    void rejectsPhenomenonRuleWithoutKeyword() {
        assertThatThrownBy(() -> extraFeeRuleService.create(new ExtraFeeRuleRequest(
                WeatherConditionType.WEATHER_PHENOMENON, VehicleType.BIKE,
                null, false, null, false, " ", new BigDecimal("1.00"), false)))
                .isInstanceOf(InvalidFeeRuleException.class);
    }

    @Test
    void rejectsRangeRuleWithMinGreaterThanMax() {
        assertThatThrownBy(() -> extraFeeRuleService.create(new ExtraFeeRuleRequest(
                WeatherConditionType.WIND_SPEED, VehicleType.BIKE,
                new BigDecimal("20"), true, new BigDecimal("10"), true, null, new BigDecimal("1.00"), false)))
                .isInstanceOf(InvalidFeeRuleException.class);
    }

    @Test
    void rejectsRangeRuleWithoutBounds() {
        assertThatThrownBy(() -> extraFeeRuleService.create(new ExtraFeeRuleRequest(
                WeatherConditionType.AIR_TEMPERATURE, VehicleType.BIKE,
                null, false, null, false, null, new BigDecimal("1.00"), false)))
                .isInstanceOf(InvalidFeeRuleException.class);
    }

    private RegionalBaseFeeResponse findActiveBaseFee(City city, VehicleType vehicleType) {
        return regionalBaseFeeService.findAllActive().stream()
                .filter(fee -> fee.city() == city && fee.vehicleType() == vehicleType)
                .findFirst()
                .orElseThrow();
    }
}

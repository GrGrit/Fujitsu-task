package com.fujitsu.task.service;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.exception.VehicleForbiddenException;
import com.fujitsu.task.exception.WeatherDataNotFoundException;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import com.fujitsu.task.repository.ExtraFeeRuleRepository;
import com.fujitsu.task.repository.RegionalBaseFeeRepository;
import com.fujitsu.task.repository.WeatherObservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");
    private static final String TARTU_STATION = "Tartu-Tõravere";

    @Mock
    private RegionalBaseFeeRepository regionalBaseFeeRepository;

    @Mock
    private ExtraFeeRuleRepository extraFeeRuleRepository;

    @Mock
    private WeatherObservationRepository weatherObservationRepository;

    private DeliveryFeeService deliveryFeeService;

    @BeforeEach
    void setUp() {
        deliveryFeeService = new DeliveryFeeService(regionalBaseFeeRepository, extraFeeRuleRepository,
                weatherObservationRepository, new ExtraFeeRuleMatcher(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsBaseFeeWhenVehicleHasNoExtraFeeRules() {
        givenBaseFee(City.TARTU, VehicleType.CAR, "3.50", NOW);
        when(extraFeeRuleRepository.findAllValidAt(VehicleType.CAR, NOW)).thenReturn(List.of());

        BigDecimal fee = deliveryFeeService.calculateFee(City.TARTU, VehicleType.CAR, null);

        assertThat(fee).isEqualByComparingTo("3.50");
        verifyNoInteractions(weatherObservationRepository);
    }

    @Test
    void addsExtraFeesOfMatchingRules() {
        givenBaseFee(City.TARTU, VehicleType.BIKE, "2.50", NOW);
        givenRules(VehicleType.BIKE, NOW,
                temperatureRule("-10", "0", "0.50"),
                phenomenonRule("snow", "1.00", false));
        givenWeather(NOW, -2.1, 4.7, "Light snow shower");

        BigDecimal fee = deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null);

        assertThat(fee).isEqualByComparingTo("4.00");
    }

    @Test
    void appliesOnlyHighestFeeWhenSeveralRulesOfSameConditionMatch() {
        givenBaseFee(City.TARTU, VehicleType.BIKE, "2.50", NOW);
        givenRules(VehicleType.BIKE, NOW,
                phenomenonRule("snow", "1.00", false),
                phenomenonRule("shower", "0.50", false));
        givenWeather(NOW, 5.0, 1.0, "Light snow shower");

        BigDecimal fee = deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null);

        assertThat(fee).isEqualByComparingTo("3.50");
    }

    @Test
    void throwsWhenMatchingRuleForbidsVehicle() {
        givenBaseFee(City.TARTU, VehicleType.BIKE, "2.50", NOW);
        givenRules(VehicleType.BIKE, NOW,
                phenomenonRule("rain", "0.50", false),
                phenomenonRule("thunder", "0.00", true));
        givenWeather(NOW, 15.0, 3.0, "Thunderstorm");

        assertThatThrownBy(() -> deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null))
                .isInstanceOf(VehicleForbiddenException.class)
                .hasMessage("Usage of selected vehicle type is forbidden");
    }

    @Test
    void usesGivenDateTimeInsteadOfCurrentTime() {
        Instant past = Instant.parse("2025-12-01T08:00:00Z");
        givenBaseFee(City.TARTU, VehicleType.BIKE, "2.50", past);
        givenRules(VehicleType.BIKE, past, temperatureRule("-10", "0", "0.50"));
        givenWeather(past, -3.0, 1.0, "Clear");

        BigDecimal fee = deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, past);

        assertThat(fee).isEqualByComparingTo("3.00");
        verify(weatherObservationRepository).findLatestAt(TARTU_STATION, past);
    }

    @Test
    void throwsWhenBaseFeeIsMissing() {
        when(regionalBaseFeeRepository.findValidAt(City.TARTU, VehicleType.BIKE, NOW)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null))
                .isInstanceOf(FeeRuleNotFoundException.class);
    }

    @Test
    void throwsWhenWeatherDataIsMissing() {
        givenBaseFee(City.TARTU, VehicleType.BIKE, "2.50", NOW);
        givenRules(VehicleType.BIKE, NOW, temperatureRule("-10", "0", "0.50"));
        when(weatherObservationRepository.findLatestAt(TARTU_STATION, NOW)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null))
                .isInstanceOf(WeatherDataNotFoundException.class);
    }

    private void givenBaseFee(City city, VehicleType vehicleType, String fee, Instant moment) {
        RegionalBaseFee baseFee = RegionalBaseFee.builder()
                .city(city)
                .vehicleType(vehicleType)
                .fee(new BigDecimal(fee))
                .validFrom(Instant.EPOCH)
                .build();
        when(regionalBaseFeeRepository.findValidAt(city, vehicleType, moment)).thenReturn(Optional.of(baseFee));
    }

    private void givenRules(VehicleType vehicleType, Instant moment, ExtraFeeRule... rules) {
        when(extraFeeRuleRepository.findAllValidAt(vehicleType, moment)).thenReturn(List.of(rules));
    }

    private void givenWeather(Instant moment, double temperature, double windSpeed, String phenomenon) {
        WeatherObservation observation =
                new WeatherObservation(TARTU_STATION, "26242", temperature, windSpeed, phenomenon, moment);
        when(weatherObservationRepository.findLatestAt(TARTU_STATION, moment)).thenReturn(Optional.of(observation));
    }

    private static ExtraFeeRule temperatureRule(String min, String max, String fee) {
        return ExtraFeeRule.builder()
                .conditionType(WeatherConditionType.AIR_TEMPERATURE)
                .vehicleType(VehicleType.BIKE)
                .minValue(new BigDecimal(min))
                .minInclusive(true)
                .maxValue(new BigDecimal(max))
                .maxInclusive(true)
                .fee(new BigDecimal(fee))
                .validFrom(Instant.EPOCH)
                .build();
    }

    private static ExtraFeeRule phenomenonRule(String keyword, String fee, boolean forbidden) {
        return ExtraFeeRule.builder()
                .conditionType(WeatherConditionType.WEATHER_PHENOMENON)
                .vehicleType(VehicleType.BIKE)
                .phenomenonKeyword(keyword)
                .fee(new BigDecimal(fee))
                .forbidden(forbidden)
                .validFrom(Instant.EPOCH)
                .build();
    }
}

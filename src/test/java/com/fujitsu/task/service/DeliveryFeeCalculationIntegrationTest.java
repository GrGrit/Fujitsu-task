package com.fujitsu.task.service;

import com.fujitsu.task.config.ClockConfig;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.VehicleForbiddenException;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.repository.WeatherObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the fee calculation against the initial business rules loaded from data.sql.
 */
@DataJpaTest
@Import({DeliveryFeeService.class, ExtraFeeRuleMatcher.class, ClockConfig.class})
class DeliveryFeeCalculationIntegrationTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-01-10T12:15:00Z");
    private static final Instant MOMENT = Instant.parse("2026-01-10T12:30:00Z");

    @Autowired
    private DeliveryFeeService deliveryFeeService;

    @Autowired
    private WeatherObservationRepository weatherObservationRepository;

    @Test
    void calculatesExampleFromSpecification() {
        givenWeather(City.TARTU, -2.1, 4.7, "Light snow shower");

        assertThat(deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, MOMENT)).isEqualByComparingTo("4.00");
    }

    @ParameterizedTest(name = "{0} {1}, {2} °C, {3} m/s, \"{4}\" -> {5} €")
    @CsvSource({
            // base fees, no extra fee conditions
            "TALLINN, CAR,      5.0,  5.0, Clear,             4.00",
            "TALLINN, SCOOTER,  5.0,  5.0, Clear,             3.50",
            "PARNU,   BIKE,     5.0,  5.0, Clear,             2.00",
            // car never gets extra fees
            "TALLINN, CAR,    -20.0, 25.0, Heavy snowfall,    4.00",
            // air temperature boundaries
            "TARTU,   SCOOTER, -10.1,  0.0, Clear,            4.00",
            "TARTU,   SCOOTER, -10.0,  0.0, Clear,            3.50",
            "TARTU,   SCOOTER,   0.0,  0.0, Clear,            3.50",
            "TARTU,   SCOOTER,   0.1,  0.0, Clear,            3.00",
            // wind speed boundaries (bike only)
            "TARTU,   BIKE,      5.0,  9.9, Clear,            2.50",
            "TARTU,   BIKE,      5.0, 10.0, Clear,            3.00",
            "TARTU,   BIKE,      5.0, 20.0, Clear,            3.00",
            "TARTU,   SCOOTER,   5.0, 25.0, Clear,            3.00",
            // weather phenomena
            "TARTU,   SCOOTER,   5.0,  0.0, Light sleet,      4.00",
            "TARTU,   SCOOTER,   5.0,  0.0, Moderate rain,    3.50",
            "TARTU,   SCOOTER,   5.0,  0.0, Light shower,     3.50",
            "TARTU,   SCOOTER,   5.0,  0.0, Mist,             3.00",
    })
    void calculatesFeeWithInitialRules(City city, VehicleType vehicleType, double temperature, double windSpeed,
                                       String phenomenon, String expectedFee) {
        givenWeather(city, temperature, windSpeed, phenomenon);

        assertThat(deliveryFeeService.calculateFee(city, vehicleType, MOMENT)).isEqualByComparingTo(expectedFee);
    }

    @ParameterizedTest(name = "{0}, {1} m/s, \"{2}\" -> forbidden")
    @CsvSource({
            "BIKE,    20.1, Clear",
            "BIKE,     5.0, Glaze",
            "SCOOTER,  5.0, Hail",
            "SCOOTER,  5.0, Thunderstorm",
    })
    void forbidsVehicleInDangerousWeather(VehicleType vehicleType, double windSpeed, String phenomenon) {
        givenWeather(City.TALLINN, 5.0, windSpeed, phenomenon);

        assertThatThrownBy(() -> deliveryFeeService.calculateFee(City.TALLINN, vehicleType, MOMENT))
                .isInstanceOf(VehicleForbiddenException.class);
    }

    private void givenWeather(City city, double temperature, double windSpeed, String phenomenon) {
        weatherObservationRepository.save(new WeatherObservation(
                city.getStationName(), "0", temperature, windSpeed, phenomenon, OBSERVED_AT));
    }
}

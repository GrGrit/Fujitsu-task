package com.fujitsu.task.repository;

import com.fujitsu.task.entity.WeatherObservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WeatherObservationRepositoryTest {

    private static final String TARTU = "Tartu-Tõravere";
    private static final Instant T1 = Instant.parse("2026-01-10T10:15:00Z");
    private static final Instant T2 = Instant.parse("2026-01-10T11:15:00Z");

    @Autowired
    private WeatherObservationRepository repository;

    @Test
    void returnsLatestObservationOfStation() {
        repository.save(observation(TARTU, -5.0, T1));
        repository.save(observation(TARTU, -2.1, T2));
        repository.save(observation("Pärnu", 3.0, T2));

        Optional<WeatherObservation> result = repository.findLatestAt(TARTU, Instant.parse("2026-01-10T12:00:00Z"));

        assertThat(result).get().extracting(WeatherObservation::getAirTemperature).isEqualTo(-2.1);
    }

    @Test
    void returnsObservationValidAtGivenMoment() {
        repository.save(observation(TARTU, -5.0, T1));
        repository.save(observation(TARTU, -2.1, T2));

        Optional<WeatherObservation> result = repository.findLatestAt(TARTU, Instant.parse("2026-01-10T10:30:00Z"));

        assertThat(result).get().extracting(WeatherObservation::getAirTemperature).isEqualTo(-5.0);
    }

    @Test
    void returnsEmptyWhenNoObservationBeforeMoment() {
        repository.save(observation(TARTU, -5.0, T1));

        Optional<WeatherObservation> result = repository.findLatestAt(TARTU, Instant.parse("2026-01-10T09:00:00Z"));

        assertThat(result).isEmpty();
    }

    private static WeatherObservation observation(String station, double temperature, Instant timestamp) {
        return new WeatherObservation(station, "26242", temperature, 4.7, "Light snow shower", timestamp);
    }
}

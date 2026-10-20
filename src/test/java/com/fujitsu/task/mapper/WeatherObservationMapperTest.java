package com.fujitsu.task.mapper;

import com.fujitsu.task.client.dto.StationObservation;
import com.fujitsu.task.entity.WeatherObservation;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherObservationMapperTest {

    private final WeatherObservationMapper mapper = new WeatherObservationMapper();

    @Test
    void toEntityMapsAllFields() {
        Instant timestamp = Instant.ofEpochSecond(1767960900L);
        StationObservation station = new StationObservation("Tartu-Tõravere", "26242", "Light snow shower", -2.1, 4.7);

        WeatherObservation entity = mapper.toEntity(station, timestamp);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getStationName()).isEqualTo("Tartu-Tõravere");
        assertThat(entity.getWmoCode()).isEqualTo("26242");
        assertThat(entity.getAirTemperature()).isEqualTo(-2.1);
        assertThat(entity.getWindSpeed()).isEqualTo(4.7);
        assertThat(entity.getWeatherPhenomenon()).isEqualTo("Light snow shower");
        assertThat(entity.getObservationTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void toEntityKeepsMissingValuesAsNull() {
        StationObservation station = new StationObservation("Pärnu", null, null, 11.4, null);

        WeatherObservation entity = mapper.toEntity(station, Instant.ofEpochSecond(1767960900L));

        assertThat(entity.getWmoCode()).isNull();
        assertThat(entity.getWeatherPhenomenon()).isNull();
        assertThat(entity.getWindSpeed()).isNull();
    }
}

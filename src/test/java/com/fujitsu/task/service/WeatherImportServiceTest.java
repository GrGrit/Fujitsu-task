package com.fujitsu.task.service;

import com.fujitsu.task.client.WeatherApiClient;
import com.fujitsu.task.client.dto.StationObservation;
import com.fujitsu.task.client.dto.WeatherObservationsResponse;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.WeatherApiException;
import com.fujitsu.task.mapper.WeatherObservationMapper;
import com.fujitsu.task.repository.WeatherObservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherImportServiceTest {

    private static final long TIMESTAMP = 1767960900L;

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private WeatherObservationRepository weatherObservationRepository;

    @InjectMocks
    private WeatherImportService weatherImportService;

    @Test
    void savesOnlyTrackedStations() {
        when(weatherApiClient.fetchObservations()).thenReturn(new WeatherObservationsResponse(TIMESTAMP, List.of(
                station("Kuressaare linn"),
                station("Tallinn-Harku"),
                station("Tartu-Tõravere"),
                station("Pärnu"),
                station("Pärnu-Sauga"))));
        when(weatherObservationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<WeatherObservation> saved = weatherImportService.importObservations();

        assertThat(saved)
                .extracting(WeatherObservation::getStationName)
                .containsExactly("Tallinn-Harku", "Tartu-Tõravere", "Pärnu");
    }

    @Test
    void mapsStationDataAndFeedTimestamp() {
        when(weatherApiClient.fetchObservations()).thenReturn(new WeatherObservationsResponse(TIMESTAMP, List.of(
                new StationObservation("Tartu-Tõravere", "26242", "Light snow shower", -2.1, 4.7))));
        when(weatherObservationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        WeatherObservation saved = weatherImportService.importObservations().getFirst();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getWmoCode()).isEqualTo("26242");
        assertThat(saved.getAirTemperature()).isEqualTo(-2.1);
        assertThat(saved.getWindSpeed()).isEqualTo(4.7);
        assertThat(saved.getWeatherPhenomenon()).isEqualTo("Light snow shower");
        assertThat(saved.getObservationTimestamp()).isEqualTo(Instant.ofEpochSecond(TIMESTAMP));
    }

    @Test
    void savesNothingWhenFeedHasNoStations() {
        when(weatherApiClient.fetchObservations()).thenReturn(new WeatherObservationsResponse(TIMESTAMP, null));
        when(weatherObservationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(weatherImportService.importObservations()).isEmpty();
    }

    @Test
    void throwsWhenTimestampIsMissing() {
        when(weatherApiClient.fetchObservations()).thenReturn(new WeatherObservationsResponse(null, List.of()));

        assertThatThrownBy(() -> weatherImportService.importObservations())
                .isInstanceOf(WeatherApiException.class);
        verifyNoInteractions(weatherObservationRepository);
    }

    private static StationObservation station(String name) {
        return new StationObservation(name, "1", "Clear", 5.0, 3.0);
    }
}

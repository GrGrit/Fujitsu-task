package com.fujitsu.task.service;

import com.fujitsu.task.client.WeatherApiClient;
import com.fujitsu.task.client.dto.WeatherObservationsResponse;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.WeatherApiException;
import com.fujitsu.task.mapper.WeatherObservationMapper;
import com.fujitsu.task.model.City;
import com.fujitsu.task.repository.WeatherObservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Imports weather observations of the stations used for delivery fee calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherImportService {

    private static final Set<String> TRACKED_STATIONS = Arrays.stream(City.values())
            .map(City::getStationName)
            .collect(Collectors.toUnmodifiableSet());

    private final WeatherApiClient weatherApiClient;
    private final WeatherObservationRepository weatherObservationRepository;
    private final WeatherObservationMapper weatherObservationMapper;

    /**
     * Fetches the current observations and stores those of the tracked stations as new rows.
     */
    @Transactional
    public List<WeatherObservation> importObservations() {
        WeatherObservationsResponse response = weatherApiClient.fetchObservations();
        if (response.timestamp() == null) {
            throw new WeatherApiException("Weather observations XML has no timestamp");
        }
        Instant timestamp = Instant.ofEpochSecond(response.timestamp());

        List<WeatherObservation> observations = response.stations() == null ? List.of() : response.stations().stream()
                .filter(station -> TRACKED_STATIONS.contains(station.name()))
                .map(station -> weatherObservationMapper.toEntity(station, timestamp))
                .toList();

        if (observations.size() < TRACKED_STATIONS.size()) {
            log.warn("Expected {} tracked stations in weather data but found {}",
                    TRACKED_STATIONS.size(), observations.size());
        }
        return weatherObservationRepository.saveAll(observations);
    }
}

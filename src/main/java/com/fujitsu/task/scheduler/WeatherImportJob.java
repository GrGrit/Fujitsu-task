package com.fujitsu.task.scheduler;

import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.WeatherApiException;
import com.fujitsu.task.service.WeatherImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodically imports weather data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherImportJob {

    private final WeatherImportService weatherImportService;

    @Value("${weather.import.on-startup:true}")
    private boolean importOnStartup;

    /**
     * Runs the import once when the application has started, so fee calculation has data
     * before the first scheduled run.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void importOnStartup() {
        if (importOnStartup) {
            importWeatherData();
        }
    }

    /**
     * Imports weather data on the configured schedule. Failures are logged.
     */
    @Scheduled(cron = "${weather.import.cron}")
    public void importWeatherData() {
        try {
            List<WeatherObservation> saved = weatherImportService.importObservations();
            log.info("Imported {} weather observations", saved.size());
        } catch (WeatherApiException e) {
            log.error("Weather data import failed: {}", e.getMessage(), e);
        }
    }
}

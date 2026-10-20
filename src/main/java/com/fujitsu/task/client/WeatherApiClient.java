package com.fujitsu.task.client;

import com.fujitsu.task.client.dto.WeatherObservationsResponse;
import com.fujitsu.task.exception.WeatherApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;

/**
 * HTTP client for the observations XML feed of the Estonian Environment Agency.
 */
@Component
public class WeatherApiClient {

    private final RestClient restClient;
    private final XmlMapper xmlMapper = XmlMapper.builder()
            .enable(XmlReadFeature.EMPTY_ELEMENT_AS_NULL)
            .build();


    public WeatherApiClient(RestClient.Builder restClientBuilder, @Value("${weather.import.url}") String url) {
        this.restClient = restClientBuilder.baseUrl(url).build();
    }

    /**
     * Downloads and parses the current observations of all stations.
     */
    public WeatherObservationsResponse fetchObservations() {
        // Read raw bytes so the XML parser takes the encoding from the XML declaration, not the HTTP header
        byte[] xml;
        try {
            xml = restClient.get().retrieve().body(byte[].class);
        } catch (RestClientException e) {
            throw new WeatherApiException("Failed to fetch weather observations", e);
        }
        if (xml == null || xml.length == 0) {
            throw new WeatherApiException("Weather service returned an empty response");
        }
        try {
            return xmlMapper.readValue(xml, WeatherObservationsResponse.class);
        } catch (JacksonException e) {
            throw new WeatherApiException("Failed to parse weather observations XML", e);
        }
    }
}

package com.fujitsu.task.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Root element of the Estonian Environment Agency observations XML.
 *
 * @param timestamp observation time of all stations, as Unix epoch seconds
 * @param stations  observations of all stations in the feed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherObservationsResponse(
        @JacksonXmlProperty(isAttribute = true, localName = "timestamp")
        Long timestamp,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "station")
        List<StationObservation> stations
) {
}

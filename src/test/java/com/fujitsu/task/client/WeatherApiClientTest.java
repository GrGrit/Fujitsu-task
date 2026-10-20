package com.fujitsu.task.client;

import com.fujitsu.task.client.dto.StationObservation;
import com.fujitsu.task.client.dto.WeatherObservationsResponse;
import com.fujitsu.task.exception.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherApiClientTest {

    private static final String URL = "https://weather.test/observations.php";

    private MockRestServiceServer server;
    private WeatherApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new WeatherApiClient(builder, URL);
    }

    @Test
    void parsesObservationsXml() throws IOException {
        server.expect(requestTo(URL)).andRespond(withSuccess(readFixture(), MediaType.TEXT_XML));

        WeatherObservationsResponse response = client.fetchObservations();

        assertThat(response.timestamp()).isEqualTo(1767960900L);
        assertThat(response.stations()).hasSize(4);
        assertThat(response.stations().get(2))
                .isEqualTo(new StationObservation("Tartu-Tõravere", "26242", "Light snow shower", -2.1, 4.7));
    }

    @Test
    void mapsEmptyElementsToNull() throws IOException {
        server.expect(requestTo(URL)).andRespond(withSuccess(readFixture(), MediaType.TEXT_XML));

        StationObservation parnu = client.fetchObservations().stations().get(3);

        assertThat(parnu.phenomenon()).isNull();
        assertThat(parnu.windSpeed()).isNull();
        assertThat(parnu.airTemperature()).isEqualTo(11.4);
    }

    @Test
    void throwsWhenServiceReturnsError() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(client::fetchObservations)
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("fetch");
    }

    @Test
    void throwsWhenResponseIsNotValidXml() {
        server.expect(requestTo(URL)).andRespond(withSuccess("<observations><station>", MediaType.TEXT_XML));

        assertThatThrownBy(client::fetchObservations)
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("parse");
    }

    @Test
    void throwsWhenResponseIsEmpty() {
        server.expect(requestTo(URL)).andRespond(withSuccess("", MediaType.TEXT_XML));

        assertThatThrownBy(client::fetchObservations)
                .isInstanceOf(WeatherApiException.class)
                .hasMessageContaining("empty");
    }

    private static String readFixture() throws IOException {
        return new ClassPathResource("weather/observations.xml").getContentAsString(StandardCharsets.UTF_8);
    }
}

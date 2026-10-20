package com.fujitsu.task.controller;

import com.fujitsu.task.exception.VehicleForbiddenException;
import com.fujitsu.task.exception.WeatherDataNotFoundException;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.service.DeliveryFeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryFeeController.class)
class DeliveryFeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryFeeService deliveryFeeService;

    @Test
    void returnsCalculatedFee() throws Exception {
        when(deliveryFeeService.calculateFee(City.TARTU, VehicleType.BIKE, null)).thenReturn(new BigDecimal("4.00"));

        mockMvc.perform(get("/api/delivery-fee").param("city", "TARTU").param("vehicleType", "BIKE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("TARTU"))
                .andExpect(jsonPath("$.vehicleType").value("BIKE"))
                .andExpect(jsonPath("$.fee").value(4.00));
    }

    @Test
    void passesDateTimeToService() throws Exception {
        Instant dateTime = Instant.parse("2026-01-10T10:00:00Z");
        when(deliveryFeeService.calculateFee(City.PARNU, VehicleType.CAR, dateTime)).thenReturn(new BigDecimal("3.00"));

        mockMvc.perform(get("/api/delivery-fee")
                        .param("city", "PARNU")
                        .param("vehicleType", "CAR")
                        .param("dateTime", "2026-01-10T12:00:00+02:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fee").value(3.00));

        verify(deliveryFeeService).calculateFee(City.PARNU, VehicleType.CAR, dateTime);
    }

    @Test
    void returnsForbiddenMessageWhenVehicleIsNotAllowed() throws Exception {
        when(deliveryFeeService.calculateFee(any(), any(), isNull())).thenThrow(new VehicleForbiddenException());

        mockMvc.perform(get("/api/delivery-fee").param("city", "TALLINN").param("vehicleType", "BIKE"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Usage of selected vehicle type is forbidden"));
    }

    @Test
    void returnsServiceUnavailableWhenWeatherDataIsMissing() throws Exception {
        when(deliveryFeeService.calculateFee(any(), any(), isNull()))
                .thenThrow(new WeatherDataNotFoundException("No weather data"));

        mockMvc.perform(get("/api/delivery-fee").param("city", "TALLINN").param("vehicleType", "BIKE"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsUnknownCityAndListsAllowedValues() throws Exception {
        mockMvc.perform(get("/api/delivery-fee").param("city", "NARVA").param("vehicleType", "BIKE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("[TALLINN, TARTU, PARNU]")));
    }

    @Test
    void rejectsMissingParameter() throws Exception {
        mockMvc.perform(get("/api/delivery-fee").param("city", "TARTU"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvalidDateTime() throws Exception {
        mockMvc.perform(get("/api/delivery-fee")
                        .param("city", "TARTU")
                        .param("vehicleType", "BIKE")
                        .param("dateTime", "yesterday"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("dateTime")));
    }
}

package com.fujitsu.task.controller;

import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.exception.InvalidFeeRuleException;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import com.fujitsu.task.service.ExtraFeeRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExtraFeeRuleController.class)
class ExtraFeeRuleControllerTest {

    private static final String WIND_RULE_BODY = """
            {"conditionType": "WIND_SPEED", "vehicleType": "BIKE",
             "minValue": 15, "minInclusive": true, "maxValue": 20, "maxInclusive": true,
             "fee": 0.75, "forbidden": false}
            """;
    private static final ExtraFeeRuleResponse RESPONSE = new ExtraFeeRuleResponse(
            30L, WeatherConditionType.WIND_SPEED, VehicleType.BIKE,
            new BigDecimal("15"), true, new BigDecimal("20"), true, null,
            new BigDecimal("0.75"), false, Instant.parse("2026-01-10T12:00:00Z"), null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExtraFeeRuleService extraFeeRuleService;

    @Test
    void listsActiveRules() throws Exception {
        when(extraFeeRuleService.findAllActive()).thenReturn(List.of(RESPONSE));

        mockMvc.perform(get("/api/extra-fees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conditionType").value("WIND_SPEED"));
    }

    @Test
    void createsRule() throws Exception {
        when(extraFeeRuleService.create(any())).thenReturn(RESPONSE);

        mockMvc.perform(post("/api/extra-fees").contentType(MediaType.APPLICATION_JSON).content(WIND_RULE_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.fee").value(0.75));
    }

    @Test
    void returnsBadRequestForInconsistentRule() throws Exception {
        when(extraFeeRuleService.create(any())).thenThrow(new InvalidFeeRuleException("minValue must not be greater"));

        mockMvc.perform(post("/api/extra-fees").contentType(MediaType.APPLICATION_JSON).content(WIND_RULE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("minValue must not be greater"));
    }

    @Test
    void rejectsUnknownConditionType() throws Exception {
        mockMvc.perform(post("/api/extra-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"conditionType": "HUMIDITY", "vehicleType": "BIKE", "fee": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesRule() throws Exception {
        mockMvc.perform(delete("/api/extra-fees/30"))
                .andExpect(status().isNoContent());

        verify(extraFeeRuleService).delete(30L);
    }
}

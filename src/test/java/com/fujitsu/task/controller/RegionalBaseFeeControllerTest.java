package com.fujitsu.task.controller;

import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.exception.DuplicateFeeRuleException;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.service.RegionalBaseFeeService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegionalBaseFeeController.class)
class RegionalBaseFeeControllerTest {

    private static final String VALID_BODY = """
            {"city": "TARTU", "vehicleType": "BIKE", "fee": 2.80}
            """;
    private static final RegionalBaseFeeRequest VALID_REQUEST =
            new RegionalBaseFeeRequest(City.TARTU, VehicleType.BIKE, new BigDecimal("2.80"));
    private static final RegionalBaseFeeResponse RESPONSE = new RegionalBaseFeeResponse(
            10L, City.TARTU, VehicleType.BIKE, new BigDecimal("2.80"), Instant.parse("2026-01-10T12:00:00Z"), null);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegionalBaseFeeService regionalBaseFeeService;

    @Test
    void listsActiveRules() throws Exception {
        when(regionalBaseFeeService.findAllActive()).thenReturn(List.of(RESPONSE));

        mockMvc.perform(get("/api/base-fees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].fee").value(2.80));
    }

    @Test
    void returnsNotFoundForUnknownId() throws Exception {
        when(regionalBaseFeeService.findById(99L)).thenThrow(new FeeRuleNotFoundException("not found"));

        mockMvc.perform(get("/api/base-fees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsRule() throws Exception {
        when(regionalBaseFeeService.create(VALID_REQUEST)).thenReturn(RESPONSE);

        mockMvc.perform(post("/api/base-fees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void rejectsNegativeFee() throws Exception {
        mockMvc.perform(post("/api/base-fees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"city": "TARTU", "vehicleType": "BIKE", "fee": -1}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(regionalBaseFeeService);
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mockMvc.perform(post("/api/base-fees").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(regionalBaseFeeService);
    }

    @Test
    void returnsConflictForDuplicateRule() throws Exception {
        when(regionalBaseFeeService.create(any())).thenThrow(new DuplicateFeeRuleException("exists"));

        mockMvc.perform(post("/api/base-fees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void updatesRule() throws Exception {
        when(regionalBaseFeeService.update(eq(6L), any())).thenReturn(RESPONSE);

        mockMvc.perform(put("/api/base-fees/6").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deletesRule() throws Exception {
        mockMvc.perform(delete("/api/base-fees/6"))
                .andExpect(status().isNoContent());

        verify(regionalBaseFeeService).delete(6L);
    }

    @Test
    void returnsNotFoundWhenDeletingInactiveRule() throws Exception {
        doThrow(new FeeRuleNotFoundException("not found")).when(regionalBaseFeeService).delete(99L);

        mockMvc.perform(delete("/api/base-fees/99"))
                .andExpect(status().isNotFound());
    }
}

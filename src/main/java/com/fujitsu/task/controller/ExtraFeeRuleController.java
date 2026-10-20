package com.fujitsu.task.controller;

import com.fujitsu.task.dto.ExtraFeeRuleRequest;
import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.service.ExtraFeeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD endpoints for weather-based extra fee rules.
 */
@Tag(name = "Extra fee rules", description = "Manage weather-based extra fee rules")
@RestController
@RequestMapping("/api/extra-fees")
@RequiredArgsConstructor
public class ExtraFeeRuleController {

    private final ExtraFeeRuleService extraFeeRuleService;

    /**
     * @return all extra fee rules currently in force
     */
    @Operation(summary = "List extra fee rules currently in force")
    @GetMapping
    public List<ExtraFeeRuleResponse> getAll() {
        return extraFeeRuleService.findAllActive();
    }

    /**
     * @return the rule, including closed versions
     */
    @Operation(summary = "Get extra fee rule by id", description = "Also returns closed versions.")
    @GetMapping("/{id}")
    public ExtraFeeRuleResponse getById(@PathVariable Long id) {
        return extraFeeRuleService.findById(id);
    }

    /**
     * @return the created rule
     */
    @Operation(summary = "Create extra fee rule", description = "Range rules (AIR_TEMPERATURE, WIND_SPEED) need minValue and/or maxValue, WEATHER_PHENOMENON rules need phenomenonKeyword.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraFeeRuleResponse create(@Valid @RequestBody ExtraFeeRuleRequest request) {
        return extraFeeRuleService.create(request);
    }

    /**
     * Replaces a rule with a new version. The returned rule has a new id.
     */
    @Operation(summary = "Update extra fee rule", description = "Closes the current version and creates a new one with a new id.")
    @PutMapping("/{id}")
    public ExtraFeeRuleResponse update(@PathVariable Long id, @Valid @RequestBody ExtraFeeRuleRequest request) {
        return extraFeeRuleService.update(id, request);
    }

    /**
     * Ends the validity of a rule. It stays available for calculations at earlier points in time.
     */
    @Operation(summary = "Delete extra fee rule", description = "Ends the validity of the rule; it is still used for past calculations.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        extraFeeRuleService.delete(id);
    }
}

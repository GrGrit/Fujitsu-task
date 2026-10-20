package com.fujitsu.task.controller;

import com.fujitsu.task.dto.ExtraFeeRuleRequest;
import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.service.ExtraFeeRuleService;
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
@RestController
@RequestMapping("/api/extra-fees")
@RequiredArgsConstructor
public class ExtraFeeRuleController {

    private final ExtraFeeRuleService extraFeeRuleService;

    /**
     * @return all extra fee rules currently in force
     */
    @GetMapping
    public List<ExtraFeeRuleResponse> getAll() {
        return extraFeeRuleService.findAllActive();
    }

    /**
     * @return the rule, including closed versions
     */
    @GetMapping("/{id}")
    public ExtraFeeRuleResponse getById(@PathVariable Long id) {
        return extraFeeRuleService.findById(id);
    }

    /**
     * @return the created rule
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExtraFeeRuleResponse create(@Valid @RequestBody ExtraFeeRuleRequest request) {
        return extraFeeRuleService.create(request);
    }

    /**
     * Replaces a rule with a new version. The returned rule has a new id.
     */
    @PutMapping("/{id}")
    public ExtraFeeRuleResponse update(@PathVariable Long id, @Valid @RequestBody ExtraFeeRuleRequest request) {
        return extraFeeRuleService.update(id, request);
    }

    /**
     * Ends the validity of a rule. It stays available for calculations at earlier points in time.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        extraFeeRuleService.delete(id);
    }
}

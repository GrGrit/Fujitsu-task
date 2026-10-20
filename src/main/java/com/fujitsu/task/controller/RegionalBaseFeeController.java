package com.fujitsu.task.controller;

import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.service.RegionalBaseFeeService;
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
 * CRUD endpoints for regional base fee rules.
 */
@Tag(name = "Regional base fees", description = "Manage regional base fee rules")
@RestController
@RequestMapping("/api/base-fees")
@RequiredArgsConstructor
public class RegionalBaseFeeController {

    private final RegionalBaseFeeService regionalBaseFeeService;

    /**
     * @return all base fee rules currently in force
     */
    @Operation(summary = "List base fees currently in force")
    @GetMapping
    public List<RegionalBaseFeeResponse> getAll() {
        return regionalBaseFeeService.findAllActive();
    }

    /**
     * @return the rule, including closed versions
     */
    @Operation(summary = "Get base fee by id", description = "Also returns closed versions.")
    @GetMapping("/{id}")
    public RegionalBaseFeeResponse getById(@PathVariable Long id) {
        return regionalBaseFeeService.findById(id);
    }

    /**
     * @return the created rule
     */
    @Operation(summary = "Create base fee", description = "Fails with 409 if a fee for the city and vehicle type is already in force.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegionalBaseFeeResponse create(@Valid @RequestBody RegionalBaseFeeRequest request) {
        return regionalBaseFeeService.create(request);
    }

    /**
     * Replaces a rule with a new version. The returned rule has a new id.
     */
    @Operation(summary = "Update base fee", description = "Closes the current version and creates a new one with a new id.")
    @PutMapping("/{id}")
    public RegionalBaseFeeResponse update(@PathVariable Long id, @Valid @RequestBody RegionalBaseFeeRequest request) {
        return regionalBaseFeeService.update(id, request);
    }

    /**
     * Ends the validity of a rule. It stays available for calculations at earlier points in time.
     */
    @Operation(summary = "Delete base fee", description = "Ends the validity of the rule; it is still used for past calculations.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        regionalBaseFeeService.delete(id);
    }
}

package com.fujitsu.task.controller;

import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.service.RegionalBaseFeeService;
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
@RestController
@RequestMapping("/api/base-fees")
@RequiredArgsConstructor
public class RegionalBaseFeeController {

    private final RegionalBaseFeeService regionalBaseFeeService;

    /**
     * @return all base fee rules currently in force
     */
    @GetMapping
    public List<RegionalBaseFeeResponse> getAll() {
        return regionalBaseFeeService.findAllActive();
    }

    /**
     * @return the rule, including closed versions
     */
    @GetMapping("/{id}")
    public RegionalBaseFeeResponse getById(@PathVariable Long id) {
        return regionalBaseFeeService.findById(id);
    }

    /**
     * @return the created rule
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegionalBaseFeeResponse create(@Valid @RequestBody RegionalBaseFeeRequest request) {
        return regionalBaseFeeService.create(request);
    }

    /**
     * Replaces a rule with a new version. The returned rule has a new id.
     */
    @PutMapping("/{id}")
    public RegionalBaseFeeResponse update(@PathVariable Long id, @Valid @RequestBody RegionalBaseFeeRequest request) {
        return regionalBaseFeeService.update(id, request);
    }

    /**
     * Ends the validity of a rule. It stays available for calculations at earlier points in time.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        regionalBaseFeeService.delete(id);
    }
}

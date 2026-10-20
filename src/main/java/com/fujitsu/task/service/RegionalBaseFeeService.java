package com.fujitsu.task.service;

import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.exception.DuplicateFeeRuleException;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.mapper.RegionalBaseFeeMapper;
import com.fujitsu.task.repository.RegionalBaseFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Manages regional base fee rules. Updating a rule closes the current version and creates a new one,
 * deleting a rule closes it. This keeps the rule history needed for calculating fees at a past point in time.
 */
@Service
@RequiredArgsConstructor
public class RegionalBaseFeeService {

    private final RegionalBaseFeeRepository repository;
    private final RegionalBaseFeeMapper mapper;
    private final Clock clock;

    /**
     * @return all base fee rules currently in force
     */
    @Transactional(readOnly = true)
    public List<RegionalBaseFeeResponse> findAllActive() {
        return repository.findAllByValidToIsNull().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Finds a rule by id, including closed versions.
     */
    @Transactional(readOnly = true)
    public RegionalBaseFeeResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    /**
     * Creates a new rule that is in force from now on.
     */
    @Transactional
    public RegionalBaseFeeResponse create(RegionalBaseFeeRequest request) {
        ensureNoActiveRule(request);
        return mapper.toResponse(repository.save(mapper.toEntity(request, clock.instant())));
    }

    /**
     * Replaces a rule currently in force with a new version. The old version stays stored with its end time set.
     */
    @Transactional
    public RegionalBaseFeeResponse update(Long id, RegionalBaseFeeRequest request) {
        RegionalBaseFee current = findActive(id);
        boolean combinationChanged = current.getCity() != request.city()
                || current.getVehicleType() != request.vehicleType();
        if (combinationChanged) {
            ensureNoActiveRule(request);
        }

        Instant now = clock.instant();
        current.setValidTo(now);
        return mapper.toResponse(repository.save(mapper.toEntity(request, now)));
    }

    /**
     * Ends the validity of a rule currently in force. The rule stays stored for historical calculations.
     */
    @Transactional
    public void delete(Long id) {
        findActive(id).setValidTo(clock.instant());
    }

    private RegionalBaseFee findActive(Long id) {
        return repository.findByIdAndValidToIsNull(id).orElseThrow(() -> notFound(id));
    }

    private void ensureNoActiveRule(RegionalBaseFeeRequest request) {
        if (repository.existsByCityAndVehicleTypeAndValidToIsNull(request.city(), request.vehicleType())) {
            throw new DuplicateFeeRuleException(
                    "Base fee for " + request.city() + " and " + request.vehicleType() + " already exists");
        }
    }

    private static FeeRuleNotFoundException notFound(Long id) {
        return new FeeRuleNotFoundException("Base fee with id " + id + " not found or no longer in force");
    }
}

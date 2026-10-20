package com.fujitsu.task.service;

import com.fujitsu.task.dto.ExtraFeeRuleRequest;
import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.exception.InvalidFeeRuleException;
import com.fujitsu.task.mapper.ExtraFeeRuleMapper;
import com.fujitsu.task.model.WeatherConditionType;
import com.fujitsu.task.repository.ExtraFeeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Manages weather-based extra fee rules. Rules are versioned the same way as base fees: updating closes the
 * current version and creates a new one, deleting closes the rule.
 */
@Service
@RequiredArgsConstructor
public class ExtraFeeRuleService {

    private final ExtraFeeRuleRepository repository;
    private final ExtraFeeRuleMapper mapper;
    private final Clock clock;

    /**
     * @return all extra fee rules currently in force
     */
    @Transactional(readOnly = true)
    public List<ExtraFeeRuleResponse> findAllActive() {
        return repository.findAllByValidToIsNull().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Finds a rule by id, including closed versions.
     */
    @Transactional(readOnly = true)
    public ExtraFeeRuleResponse findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> notFound(id));
    }

    /**
     * Creates a new rule that is in force from now on.
     */
    @Transactional
    public ExtraFeeRuleResponse create(ExtraFeeRuleRequest request) {
        validate(request);
        return mapper.toResponse(repository.save(mapper.toEntity(request, clock.instant())));
    }

    /**
     * Replaces a rule currently in force with a new version. The old version stays stored with its end time set.
     */
    @Transactional
    public ExtraFeeRuleResponse update(Long id, ExtraFeeRuleRequest request) {
        validate(request);
        ExtraFeeRule current = findActive(id);

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

    private ExtraFeeRule findActive(Long id) {
        return repository.findByIdAndValidToIsNull(id).orElseThrow(() -> notFound(id));
    }

    private static void validate(ExtraFeeRuleRequest request) {
        if (request.conditionType() == WeatherConditionType.WEATHER_PHENOMENON) {
            if (request.phenomenonKeyword() == null || request.phenomenonKeyword().isBlank()) {
                throw new InvalidFeeRuleException("phenomenonKeyword is required for WEATHER_PHENOMENON rules");
            }
            return;
        }
        if (request.minValue() == null && request.maxValue() == null) {
            throw new InvalidFeeRuleException("minValue or maxValue is required for " + request.conditionType() + " rules");
        }
        if (request.minValue() != null && request.maxValue() != null
                && request.minValue().compareTo(request.maxValue()) > 0) {
            throw new InvalidFeeRuleException("minValue must not be greater than maxValue");
        }
    }

    private static FeeRuleNotFoundException notFound(Long id) {
        return new FeeRuleNotFoundException("Extra fee rule with id " + id + " not found or no longer in force");
    }
}

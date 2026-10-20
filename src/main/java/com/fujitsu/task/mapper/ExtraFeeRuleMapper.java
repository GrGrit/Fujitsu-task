package com.fujitsu.task.mapper;

import com.fujitsu.task.dto.ExtraFeeRuleRequest;
import com.fujitsu.task.dto.ExtraFeeRuleResponse;
import com.fujitsu.task.entity.ExtraFeeRule;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts between {@link ExtraFeeRule} entities and their REST representations.
 */
@Component
public class ExtraFeeRuleMapper {

    /**
     * Creates a new, unsaved rule version from request data.
     */
    public ExtraFeeRule toEntity(ExtraFeeRuleRequest request, Instant validFrom) {
        return ExtraFeeRule.builder()
                .conditionType(request.conditionType())
                .vehicleType(request.vehicleType())
                .minValue(request.minValue())
                .minInclusive(request.minInclusive())
                .maxValue(request.maxValue())
                .maxInclusive(request.maxInclusive())
                .phenomenonKeyword(request.phenomenonKeyword())
                .fee(request.fee())
                .forbidden(request.forbidden())
                .validFrom(validFrom)
                .build();
    }

    /**
     * @return REST representation of the rule
     */
    public ExtraFeeRuleResponse toResponse(ExtraFeeRule entity) {
        return new ExtraFeeRuleResponse(
                entity.getId(),
                entity.getConditionType(),
                entity.getVehicleType(),
                entity.getMinValue(),
                entity.isMinInclusive(),
                entity.getMaxValue(),
                entity.isMaxInclusive(),
                entity.getPhenomenonKeyword(),
                entity.getFee(),
                entity.isForbidden(),
                entity.getValidFrom(),
                entity.getValidTo());
    }
}

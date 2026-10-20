package com.fujitsu.task.mapper;

import com.fujitsu.task.dto.RegionalBaseFeeRequest;
import com.fujitsu.task.dto.RegionalBaseFeeResponse;
import com.fujitsu.task.entity.RegionalBaseFee;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts between {@link RegionalBaseFee} entities and their REST representations.
 */
@Component
public class RegionalBaseFeeMapper {

    /**
     * Creates a new, unsaved rule version from request data.
     */
    public RegionalBaseFee toEntity(RegionalBaseFeeRequest request, Instant validFrom) {
        return RegionalBaseFee.builder()
                .city(request.city())
                .vehicleType(request.vehicleType())
                .fee(request.fee())
                .validFrom(validFrom)
                .build();
    }

    /**
     * @return REST representation of the rule
     */
    public RegionalBaseFeeResponse toResponse(RegionalBaseFee entity) {
        return new RegionalBaseFeeResponse(
                entity.getId(),
                entity.getCity(),
                entity.getVehicleType(),
                entity.getFee(),
                entity.getValidFrom(),
                entity.getValidTo());
    }
}

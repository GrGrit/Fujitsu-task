package com.fujitsu.task.service;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.entity.RegionalBaseFee;
import com.fujitsu.task.entity.WeatherObservation;
import com.fujitsu.task.exception.FeeRuleNotFoundException;
import com.fujitsu.task.exception.VehicleForbiddenException;
import com.fujitsu.task.exception.WeatherDataNotFoundException;
import com.fujitsu.task.model.City;
import com.fujitsu.task.model.VehicleType;
import com.fujitsu.task.model.WeatherConditionType;
import com.fujitsu.task.repository.ExtraFeeRuleRepository;
import com.fujitsu.task.repository.RegionalBaseFeeRepository;
import com.fujitsu.task.repository.WeatherObservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates delivery fees from the regional base fee and weather-based extra fees.
 */
@Service
@RequiredArgsConstructor
public class DeliveryFeeService {

    private final RegionalBaseFeeRepository regionalBaseFeeRepository;
    private final ExtraFeeRuleRepository extraFeeRuleRepository;
    private final WeatherObservationRepository weatherObservationRepository;
    private final ExtraFeeRuleMatcher extraFeeRuleMatcher;
    private final Clock clock;

    /**
     * Calculates the total delivery fee using the rules and the latest weather data valid at the given time.
     * If any matching rule forbids the vehicle, the delivery is not allowed.
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateFee(City city, VehicleType vehicleType, Instant dateTime) {
        Instant moment = dateTime != null ? dateTime : clock.instant();

        RegionalBaseFee baseFee = regionalBaseFeeRepository.findValidAt(city, vehicleType, moment)
                .orElseThrow(() -> new FeeRuleNotFoundException(
                        "No base fee found for " + city + " and " + vehicleType + " at " + moment));

        List<ExtraFeeRule> rules = extraFeeRuleRepository.findAllValidAt(vehicleType, moment);
        if (rules.isEmpty()) {
            return baseFee.getFee();
        }

        WeatherObservation observation = weatherObservationRepository.findLatestAt(city.getStationName(), moment)
                .orElseThrow(() -> new WeatherDataNotFoundException(
                        "No weather data found for " + city.getStationName() + " at " + moment));

        return baseFee.getFee().add(calculateExtraFee(rules, observation));
    }

    private BigDecimal calculateExtraFee(List<ExtraFeeRule> rules, WeatherObservation observation) {
        List<ExtraFeeRule> matchingRules = rules.stream()
                .filter(rule -> extraFeeRuleMatcher.matches(rule, observation))
                .toList();

        if (matchingRules.stream().anyMatch(ExtraFeeRule::isForbidden)) {
            throw new VehicleForbiddenException();
        }

        Map<WeatherConditionType, BigDecimal> highestFeePerCondition = matchingRules.stream()
                .collect(Collectors.toMap(ExtraFeeRule::getConditionType, ExtraFeeRule::getFee, BigDecimal::max));

        return highestFeePerCondition.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

package com.fujitsu.task.service;

import com.fujitsu.task.entity.ExtraFeeRule;
import com.fujitsu.task.entity.WeatherObservation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Decides whether an extra fee rule applies to a weather observation.
 */
@Component
public class ExtraFeeRuleMatcher {

    /**
     * Checks whether the rule's condition is met by the observation. A missing measurement never matches.
     */
    public boolean matches(ExtraFeeRule rule, WeatherObservation observation) {
        return switch (rule.getConditionType()) {
            case AIR_TEMPERATURE -> isInRange(rule, observation.getAirTemperature());
            case WIND_SPEED -> isInRange(rule, observation.getWindSpeed());
            case WEATHER_PHENOMENON -> containsKeyword(observation.getWeatherPhenomenon(), rule.getPhenomenonKeyword());
        };
    }

    private static boolean isInRange(ExtraFeeRule rule, Double measured) {
        if (measured == null) {
            return false;
        }
        BigDecimal value = BigDecimal.valueOf(measured);
        return isAboveMin(value, rule.getMinValue(), rule.isMinInclusive())
                && isBelowMax(value, rule.getMaxValue(), rule.isMaxInclusive());
    }

    private static boolean isAboveMin(BigDecimal value, BigDecimal min, boolean inclusive) {
        if (min == null) {
            return true;
        }
        int comparison = value.compareTo(min);
        return inclusive ? comparison >= 0 : comparison > 0;
    }

    private static boolean isBelowMax(BigDecimal value, BigDecimal max, boolean inclusive) {
        if (max == null) {
            return true;
        }
        int comparison = value.compareTo(max);
        return inclusive ? comparison <= 0 : comparison < 0;
    }

    private static boolean containsKeyword(String phenomenon, String keyword) {
        if (phenomenon == null || keyword == null) {
            return false;
        }
        return phenomenon.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}

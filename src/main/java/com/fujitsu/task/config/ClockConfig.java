package com.fujitsu.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides the clock used to determine the current time, so it can be replaced with a fixed clock in tests.
 */
@Configuration
public class ClockConfig {

    /**
     * System clock in UTC.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

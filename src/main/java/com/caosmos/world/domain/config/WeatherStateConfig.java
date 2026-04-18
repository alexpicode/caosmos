package com.caosmos.world.domain.config;

import java.util.Map;

/**
 * Configuration for a specific weather state, including duration ranges and transitions to other states.
 */
public record WeatherStateConfig(
    double minDurationMinutes,
    double maxDurationMinutes,
    Map<String, Double> transitions
) {

}

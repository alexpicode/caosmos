package com.caosmos.world.infrastructure.config;

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

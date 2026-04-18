package com.caosmos.world.infrastructure.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caosmos.world")
public record WorldConfigProperties(
    Map<String, WeatherStateConfig> weatherStates
) {

}

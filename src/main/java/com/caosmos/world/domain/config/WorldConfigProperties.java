package com.caosmos.world.domain.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caosmos.world")
public record WorldConfigProperties(
    Map<String, WeatherStateConfig> weatherStates
) {

}

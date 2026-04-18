package com.caosmos.world.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caosmos.world")
public record WorldConfigProperties(
    List<String> weatherOptions
) {

}

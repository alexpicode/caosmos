package com.caosmos.common.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "caosmos.resources")
public record CaosmosResourceProperties(
    Resource citizens,
    WorldResources world
) {

  public record WorldResources(
      Resource zones,
      Resource objects
  ) {

  }
}

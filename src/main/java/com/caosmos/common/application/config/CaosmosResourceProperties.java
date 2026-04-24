package com.caosmos.common.application.config;

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

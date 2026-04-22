package com.caosmos.citizens.application.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for citizen behavior and settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "caosmos.citizen")
public class CitizenSettings {

  private static final int DEFAULT_MAX_TICKS_WITHOUT_DECISION = 20;

  private int pulseFrequency;
  private double walkingSpeed;
  private int maxTicksWithoutDecision = DEFAULT_MAX_TICKS_WITHOUT_DECISION;
}

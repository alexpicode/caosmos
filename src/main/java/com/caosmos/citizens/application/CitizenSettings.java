package com.caosmos.citizens.application;

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

  private int pulseFrequency;
  private double walkingSpeed;
}

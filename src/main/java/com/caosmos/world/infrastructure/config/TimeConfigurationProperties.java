package com.caosmos.world.infrastructure.config;

import com.caosmos.world.domain.contracts.WorldTimeSettings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "caosmos.world.time")
public class TimeConfigurationProperties implements WorldTimeSettings {

  private double timeScale = 60.0;       // 1 real second = X simulation seconds
  private int startDay = 1;
  private int startHour = 8;
}

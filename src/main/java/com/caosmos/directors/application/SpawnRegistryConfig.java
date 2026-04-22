package com.caosmos.directors.application;

import com.caosmos.directors.domain.model.ItemTemplate;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties
public class SpawnRegistryConfig {

  private Map<String, ItemTemplate> spawnables;
  private Map<String, String> destructionFallbacks;
}

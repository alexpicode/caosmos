package com.caosmos.common.infrastructure.manifest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "caosmos.manifests")
public class ManifestProperties {

  private String externalPath;
  private String internalPath;
}

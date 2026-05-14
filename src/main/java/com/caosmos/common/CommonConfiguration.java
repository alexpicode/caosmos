package com.caosmos.common;

import com.caosmos.common.infrastructure.ai.AiModelProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.modulith.ApplicationModule;

@ApplicationModule(type = ApplicationModule.Type.OPEN)
@EnableConfigurationProperties(AiModelProperties.class)
public class CommonConfiguration {

}

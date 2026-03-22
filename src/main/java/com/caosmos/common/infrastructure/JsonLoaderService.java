package com.caosmos.common.infrastructure;

import com.caosmos.common.domain.contracts.JsonLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonLoaderService implements JsonLoader {

  private final ObjectMapper objectMapper;

  @Override
  public <T> T load(Resource resource, Class<T> configClass) {
    try {
      log.info("Loading {} from JSON configuration", configClass.getSimpleName());

      T loadedObject = objectMapper.readValue(resource.getInputStream(), configClass);

      log.info("Successfully loaded {} object", configClass.getSimpleName());

      return loadedObject;

    } catch (IOException e) {
      log.error("Failed to load {} from JSON", configClass.getSimpleName(), e);
      throw new RuntimeException("Could not load " + configClass.getSimpleName() + " configuration", e);
    }
  }
}

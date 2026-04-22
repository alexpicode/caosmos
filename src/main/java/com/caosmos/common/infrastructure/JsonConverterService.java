package com.caosmos.common.infrastructure;

import com.caosmos.common.domain.contracts.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of JsonSerializer using Jackson ObjectMapper. Handles object-to-JSON conversion with proper error
 * handling.
 */
@Service
@RequiredArgsConstructor
public class JsonConverterService implements JsonSerializer {

  private final ObjectMapper objectMapper;

  @Override
  public String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error converting object to JSON", e);
    }
  }
}

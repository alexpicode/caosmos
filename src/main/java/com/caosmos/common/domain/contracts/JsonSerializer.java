package com.caosmos.common.domain.contracts;

/**
 * Contract for serializing objects to JSON strings. Provides a clean abstraction for JSON conversion operations.
 */
public interface JsonSerializer {

  /**
   * Converts any object to its JSON string representation.
   *
   * @param object the object to serialize
   * @return JSON string representation of the object
   * @throws RuntimeException if serialization fails
   */
  String toJson(Object object);
}

package com.caosmos.common.domain.contracts;

/**
 * Interface for elements that require periodic cleanup or updates based on simulation ticks.
 */
public interface TemporalElementManager {

  void cleanup(long currentTick);
}

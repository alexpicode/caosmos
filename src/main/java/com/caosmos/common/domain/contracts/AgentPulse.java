package com.caosmos.common.domain.contracts;

/**
 * Common interface for any entity that possesses a cognitive cycle.
 */
public interface AgentPulse {

  /**
   * Executes one cycle of perception, deliberation, and action.
   *
   * @param tick The current simulation tick provided by the MasterClock.
   */
  void pulse(long tick);
}
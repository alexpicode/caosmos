package com.caosmos.common.application.startup;

/**
 * Interface for ticker components that manage time-based simulation cycles. Provides the contract for starting the
 * ticking mechanism.
 */
public interface Ticker {

  /**
   * Starts the ticker, beginning the time-based simulation loop. This method should be non-blocking and typically runs
   * the tick loop in a separate thread or virtual thread.
   */
  void start();
}

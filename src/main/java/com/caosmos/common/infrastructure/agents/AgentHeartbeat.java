package com.caosmos.common.infrastructure.agents;

import com.caosmos.common.domain.contracts.AgentPulse;
import com.caosmos.common.domain.service.core.MasterClock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AgentHeartbeat implements Runnable {

  private final String agentId;
  private final MasterClock clock;
  private final AgentPulse mind;
  private final int frequency;
  private volatile boolean active = true;

  @Override
  public void run() {
    while (active && !Thread.currentThread().isInterrupted()) {
      try {
        clock.waitForTicks(frequency);

        // The heartbeat doesn't know WHAT it's running, just that it's time to run it
        mind.pulse(clock.getCurrentTick());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        active = false;
      }
    }
  }
}
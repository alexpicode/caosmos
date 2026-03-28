package com.caosmos.common.infrastructure;

import com.caosmos.common.application.startup.Ticker;
import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.common.domain.service.core.MasterClock;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterTicker implements Ticker {

  private static final long TICK_DURATION_MS = 1000; // 1 second per tick

  private final MasterClock clock;
  private final SimulationClock simulationClock;
  private boolean running = true;

  public void start() {
    log.info("Starting the Primordial Pulse of Caosmos...");
    Thread.ofVirtual().name("master-ticker-loop").start(this::runLoop);
  }

  private void runLoop() {
    while (running) {
      long startTime = System.currentTimeMillis();

      long nextTick = clock.getCurrentTick() + 1;

      // Update simulation time (real-world based) for the next tick
      simulationClock.update(nextTick);

      // Advance discrete physical clock
      clock.advance();

      waitForMaintenance(startTime);
    }
  }

  private void waitForMaintenance(long startTime) {
    long endTime = System.currentTimeMillis();
    long elapsed = endTime - startTime;
    long sleepTime = TICK_DURATION_MS - elapsed;

    if (sleepTime > 0) {
      try {
        TimeUnit.MILLISECONDS.sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        this.running = false;
      }
    } else if (sleepTime < -500) {
      log.warn(
          "Tick Overload! World logic took {}ms (target: {}ms)",
          elapsed, TICK_DURATION_MS
      );
    }
  }
}
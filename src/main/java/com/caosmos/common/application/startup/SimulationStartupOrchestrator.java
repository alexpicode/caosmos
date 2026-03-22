package com.caosmos.common.application.startup;

import com.caosmos.common.application.agents.PopulationService;
import com.caosmos.common.application.manifest.ManifestManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Ensures the correct order of startup for the Caosmos simulation. 1. Load Data -> 2. Spawn Agents -> 3. Start Clock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationStartupOrchestrator {

  private final ManifestManager manifestService;
  private final PopulationService populationService;
  private final Ticker masterTicker;

  /**
   * This is the "Big Bang" of your universe. It triggers only when the Spring context is fully operational.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("[STARTUP] --- Beginning Caosmos Genesis Sequence ---");

    try {
      // STEP 1: Prepare the blueprints (Infrastructure)
      // We load manifests into memory so they are available for all slices.
      log.info("[STARTUP] Step 1: Loading all agent manifests...");
      manifestService.init();

      // STEP 2: Inhabit the world (Slice Application Service)
      // This creates the Virtual Threads for each agent.
      // Note: They are now alive but waiting for the first tick.
      log.info("[STARTUP] Step 2: Spawning initial population...");
      populationService.spawnAll();

      // STEP 3: Start the flow of time (Infrastructure/Core)
      // Once the pulse starts, the agents will receive their first heartbeat.
      log.info("[STARTUP] Step 3: Starting the Master Ticker...");
      masterTicker.start();

      log.info("[STARTUP] --- Genesis Sequence Complete. Caosmos is now LIVE ---");
    } catch (Exception e) {
      log.error("[STARTUP] CRITICAL: Genesis Sequence failed!", e);
      // In a real system, you might want to call System.exit(1) here
      // if the simulation cannot start in a consistent state.
    }
  }
}
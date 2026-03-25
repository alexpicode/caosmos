package com.caosmos.citizens.application;

import com.caosmos.citizens.application.model.FullPerception;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles perception gathering and reflex evaluation for citizens. Manages citizen, world perception and reflexive
 * responses to environmental stimuli.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenPerceptionHandler {

  private final WorldPerceptionProvider worldPerceptionProvider;
  private final PerceptionMonitor perceptionMonitor;

  /**
   * Handles the perception process for a citizen. Gathers world perception and evaluates reflexes.
   */
  public FullPerception handlePerception(Citizen citizen, List<String> unprocessedEvents) {
    String citizenName = citizen.getCitizenProfile().identity().name();

    // Get current position and world perception with filter to exclude self
    Vector3 currentPosition = citizen.getCurrentState().getPosition();
    String currentZoneId = citizen.getCurrentState().getCurrentZoneId();
    WorldPerception worldPerception = worldPerceptionProvider.getPerceptionAt(
        currentPosition,
        currentZoneId,
        worldEntity -> !worldEntity.getId().equals(citizen.getId())
    );
    log.debug("[CITIZEN:{}] WorldPerception at position {}: {}", citizenName, currentPosition, worldPerception);

    // Evaluate reflexes
    ReflexResult reflex = perceptionMonitor.evaluate(
        citizen.getCurrentState(),
        citizen.getPerception().status(),
        worldPerception
    );

    // Add informative events to the provided list without duplicates
    reflex.informativeEvents().forEach(e -> {
      if (!unprocessedEvents.contains(e)) {
        unprocessedEvents.add(e);
      }
    });

    // Get citizen's current perception
    CitizenPerception citizenPerception = citizen.getPerception();

    return new FullPerception(citizenPerception, worldPerception, reflex);
  }
}

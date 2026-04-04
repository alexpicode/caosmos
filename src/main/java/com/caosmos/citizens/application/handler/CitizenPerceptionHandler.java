package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
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
  private final CitizenMentalMapper mentalMapper;

  /**
   * Handles the perception process for a citizen. Gathers world perception and evaluates reflexes.
   */
  public FullPerception handlePerception(
      Citizen citizen,
      List<String> unprocessedEvents,
      boolean allowsRoutineInterruptions
  ) {
    String citizenName = citizen.getCitizenProfile().identity().name();

    // 1. Get current position and world perception with filter to exclude self
    Vector3 currentPosition = citizen.getCurrentState().getPosition();
    String currentZoneId = citizen.getCurrentState().getCurrentZoneId();
    WorldPerception worldPerception = worldPerceptionProvider.getPerceptionAt(
        currentPosition,
        currentZoneId,
        worldEntity -> !worldEntity.getId().equals(citizen.getId())
    );
    log.debug("[CITIZEN:{}] WorldPerception at position {}: {}", citizenName, currentPosition, worldPerception);

    // 2. Synchronize Spatial Context (Zone & Mental Map)
    // We pass the already fetched worldPerception to avoid redundant calls
    synchronizeSpatialContext(citizen, currentPosition, worldPerception);

    // 3. Evaluate reflexes (pure evaluation)
    PerceptionEvaluation eval = perceptionMonitor.evaluate(
        citizen,
        worldPerception,
        allowsRoutineInterruptions
    );

    ReflexResult reflex = eval.reflex();

    // 4. Add informative events to the provided list without duplicates
    reflex.informativeEvents().forEach(e -> {
      if (!unprocessedEvents.contains(e)) {
        unprocessedEvents.add(e);
      }
    });

    return new FullPerception(citizen.getPerception(), worldPerception, reflex);
  }

  /**
   * Synchronizes the citizen's spatial context (Zone and Mental Map) with their current physical position. Useful for
   * immediate updates outside the normal pulse cycle.
   */
  public void synchronizeSpatialContext(Citizen citizen, Vector3 position) {
    String currentZoneId = citizen.getCurrentState().getCurrentZoneId();
    WorldPerception perception = worldPerceptionProvider.getPerceptionAt(
        position,
        currentZoneId,
        e -> !e.getId().equals(citizen.getId())
    );
    synchronizeSpatialContext(citizen, position, perception);
  }

  private void synchronizeSpatialContext(Citizen citizen, Vector3 position, WorldPerception perception) {
    if (perception != null && perception.location() != null) {
      citizen.enterZone(perception.location().zoneId(), perception.location().zone());
    }

    MentalMap mentalMap = mentalMapper.calculate(citizen, position);
    citizen.updateMentalMap(mentalMap);
  }
}


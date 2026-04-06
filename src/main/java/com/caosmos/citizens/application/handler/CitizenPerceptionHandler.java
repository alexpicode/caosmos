package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.world.SpeechTone;
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
  private final WorldPort worldPort;
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
    synchronizeSpatialContext(citizen, currentPosition, worldPerception);

    // 3. Extract Speech Messages
    List<SpeechMessage> messages = worldPerception.nearbyElements().stream()
        .filter(e -> "MESSAGE".equals(e.type()))
        .map(e -> {
          SpeechTone tone = e.tags().isEmpty() ? SpeechTone.NEUTRAL : SpeechTone.fromString(e.tags().iterator().next());
          return new SpeechMessage(e.id(), e.sourceId(), e.name(), e.targetId(), e.message(), tone);
        })
        .toList();

    // 4. Auto-consume direct messages
    for (SpeechMessage msg : messages) {
      if (citizen.getId().equals(msg.targetId())) {
        worldPort.consumeSpeech(msg.id());
        log.debug("Citizen {} consumed direct message from {}", citizen.getId(), msg.sourceName());
      }
    }

    // 5. Evaluate reflexes (pure evaluation)
    PerceptionEvaluation eval = perceptionMonitor.evaluate(
        citizen,
        worldPerception,
        allowsRoutineInterruptions
    );

    ReflexResult reflex = eval.reflex();

    // 6. Add informative events to the provided list without duplicates
    reflex.informativeEvents().forEach(e -> {
      if (!unprocessedEvents.contains(e)) {
        unprocessedEvents.add(e);
      }
    });

    return new FullPerception(citizen.getPerception(), worldPerception, reflex, messages);
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

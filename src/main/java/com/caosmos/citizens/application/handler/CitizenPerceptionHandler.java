package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.application.model.FullPerception;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.CognitiveAnchor;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldPerception;
import com.caosmos.world.domain.service.DirectionCalculator;
import com.caosmos.world.domain.service.NearbyZoneService;
import com.caosmos.world.domain.service.SemanticDistanceMapper;
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
  private final NearbyZoneService nearbyZoneService;
  private final DirectionCalculator directionCalculator;
  private final SemanticDistanceMapper distanceMapper;

  /**
   * Handles the perception process for a citizen. Gathers world perception and evaluates reflexes.
   */
  public FullPerception handlePerception(
      Citizen citizen,
      List<String> unprocessedEvents,
      boolean allowsRoutineInterruptions
  ) {
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

    // Evaluate reflexes (pure evaluation)
    PerceptionEvaluation eval = perceptionMonitor.evaluate(
        citizen,
        worldPerception,
        allowsRoutineInterruptions
    );

    // Apply state changes recommended by the evaluation
    if (eval.hasEnteredNewZone()) {
      citizen.enterZone(eval.newZoneId(), eval.newZoneName());
    }

    ReflexResult reflex = eval.reflex();

    // Add informative events to the provided list without duplicates
    reflex.informativeEvents().forEach(e -> {
      if (!unprocessedEvents.contains(e)) {
        unprocessedEvents.add(e);
      }
    });

    // Calculate Mental Map
    MentalMap mentalMap = calculateMentalMap(citizen, currentPosition);

    // Get citizen's current perception
    CitizenPerception citizenPerception = citizen.getPerception(mentalMap);

    return new FullPerception(citizenPerception, worldPerception, reflex);
  }

  private MentalMap calculateMentalMap(Citizen citizen, Vector3 currentPosition) {
    // 1. Home Anchor
    CognitiveAnchor homeAnchor = null;
    if (citizen.getCitizenProfile().baseLocation() != null) {
      Vector3 homePos = new Vector3(
          citizen.getCitizenProfile().baseLocation().x(),
          citizen.getCitizenProfile().baseLocation().y(),
          citizen.getCitizenProfile().baseLocation().z()
      );
      double distance = currentPosition.distanceTo2D(homePos);
      homeAnchor = new CognitiveAnchor(
          "Home",
          Math.round(distance * 100.0) / 100.0,
          distanceMapper.mapDistance(distance),
          directionCalculator.getCardinalDirection(currentPosition, homePos)
      );
    }

    // 2. Nearest City Anchor
    CognitiveAnchor cityAnchor = null;
    var nearestCityOpt = nearbyZoneService.findNearestCity(currentPosition);
    if (nearestCityOpt.isPresent()) {
      var city = nearestCityOpt.get();
      double distance = currentPosition.distanceTo2D(city.getCenter());
      cityAnchor = new CognitiveAnchor(
          city.getName(),
          Math.round(distance * 100.0) / 100.0,
          distanceMapper.mapDistance(distance),
          directionCalculator.getCardinalDirection(currentPosition, city.getCenter())
      );
    }

    return new MentalMap(homeAnchor, cityAnchor);
  }
}


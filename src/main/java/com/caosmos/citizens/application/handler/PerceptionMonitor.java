package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Monitors perception to detect critical stimuli that require immediate attention (reflexes).
 */
@Component
public class PerceptionMonitor {

  /**
   * Evaluates perception to determine if there's a critical reason to stop the current task.
   */
  public ReflexResult evaluate(
      Citizen citizen,
      WorldPerception perception,
      boolean allowsRoutineInterruptions
  ) {
    var currentState = citizen.getCurrentState();
    var status = citizen.getPerception().status();
    List<String> informativeEvents = new ArrayList<>();

    // 1. Check for zone change reflexes
    String previousZoneId = currentState.getCurrentZoneId();
    String newZoneId = perception.location().zoneId();
    String newZoneName = perception.location().zone();

    if (previousZoneId == null) {
      currentState.setCurrentZoneId(newZoneId);
      currentState.setCurrentZone(newZoneName);
      citizen.markZoneAsVisited(newZoneId);
    } else if (!previousZoneId.equals(newZoneId)) {
      currentState.setCurrentZoneId(newZoneId);
      currentState.setCurrentZone(newZoneName);

      boolean isNewZone = !citizen.isZoneVisited(newZoneId);
      citizen.markZoneAsVisited(newZoneId);

      if (isNewZone && allowsRoutineInterruptions) {
        informativeEvents.add("NOVELTY! You've entered an unexplored zone: " + newZoneName);
        return new ReflexResult(true, "Zone discovery: " + newZoneName, informativeEvents);
      }

      informativeEvents.add("You've entered the zone: " + newZoneName);
    }

    // 2. Check for critical threats or immediate social interactions
    for (NearbyEntity entity : perception.nearbyEntities()) {
      if (entity.distance() < PhysiologicalThresholds.ENTITY_PROXIMITY_ALERT_DISTANCE ||
          (entity.tags() != null && entity.tags().stream().anyMatch(t -> "hostile".equalsIgnoreCase(t)))) {
        return new ReflexResult(true, "Threat detected: " + entity.name(), informativeEvents);
      }

      if (allowsRoutineInterruptions) {
        if (entity.tags() != null && entity.tags().stream().anyMatch(t -> "INTERESTING".equalsIgnoreCase(t))) {
          informativeEvents.add("INTERESTING! You've spotted: " + entity.name());
          return new ReflexResult(true, "Object of interest: " + entity.name(), informativeEvents);
        }

        if (entity.tags() != null && entity.tags().stream().anyMatch(t -> "resource".equalsIgnoreCase(t))) {
          informativeEvents.add(
              "You've seen a resource: " + entity.name() + " at " + String.format("%.1fm", entity.distance()));
          return new ReflexResult(true, "Resource detected: " + entity.name(), informativeEvents);
        }

        informativeEvents.add("Seen " + entity.name() + " at " + String.format("%.1fm", entity.distance()));
      }
    }

    // 3. Physiological Alerts (Narrative)
    if (status.vitality() < PhysiologicalThresholds.VITALITY_CRITICAL) {
      informativeEvents.add("Your body is severely injured.");
    }
    if (status.hunger() > PhysiologicalThresholds.HUNGER_CRISIS) {
      informativeEvents.add("You are starving, you must eat now.");
    }
    if (status.energy() < PhysiologicalThresholds.ENERGY_EXTREME_FATIGUE) {
      informativeEvents.add("You are on the verge of collapse due to exhaustion.");
    }
    if (status.stress() > PhysiologicalThresholds.STRESS_CRITICAL) {
      informativeEvents.add("You are at the limit of your mental endurance.");
    }

    return new ReflexResult(false, null, informativeEvents);
  }
}

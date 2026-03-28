package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.citizens.domain.model.perception.Status;
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
      CurrentState currentState,
      Status status,
      WorldPerception perception,
      boolean allowsRoutineInterruptions
  ) {
    List<String> informativeEvents = new ArrayList<>();

    // 1. Check for zone change reflexes
    String previousZoneId = currentState.getCurrentZoneId();
    String newZoneId = perception.location().zoneId();
    String newZoneName = perception.location().zone();

    if (previousZoneId == null) {
      currentState.setCurrentZoneId(newZoneId);
      currentState.setCurrentZone(newZoneName);
    } else if (!previousZoneId.equals(newZoneId)) {
      currentState.setCurrentZoneId(newZoneId);
      currentState.setCurrentZone(newZoneName);
      if (allowsRoutineInterruptions) {
        informativeEvents.add("Has entrado a la zona: " + newZoneName);
        return new ReflexResult(true, "Llegada/Entrada a zona: " + newZoneName, informativeEvents);
      }
    }

    // 2. Check for critical threats or immediate social interactions
    for (NearbyEntity entity : perception.nearbyEntities()) {
      if (entity.distance() < PhysiologicalThresholds.ENTITY_PROXIMITY_ALERT_DISTANCE ||
          (entity.tags() != null && entity.tags().contains("HOSTILE"))) {
        return new ReflexResult(true, "Threat detected: " + entity.name(), informativeEvents);
      }

      if (allowsRoutineInterruptions) {
        if (entity.tags() != null && entity.tags().contains("RESOURCE")) {
          informativeEvents.add(
              "Has visto un recurso: " + entity.name() + " a " + String.format("%.1fm", entity.distance()));
          return new ReflexResult(true, "Recurso detectado: " + entity.name(), informativeEvents);
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

package com.caosmos.citizens.application.handler;

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
  public ReflexResult evaluate(CurrentState currentState, Status status, WorldPerception perception) {
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
      informativeEvents.add("Has entrado a la zona: " + newZoneName);
      return new ReflexResult(true, "Llegada/Entrada a zona: " + newZoneName, informativeEvents);
    }

    // 2. Check for critical threats or immediate social interactions
    for (NearbyEntity entity : perception.nearbyEntities()) {
      if (entity.distance() < 1.5) {
        return new ReflexResult(true, "Entity too close: " + entity.name(), informativeEvents);
      }
      informativeEvents.add("Seen " + entity.name() + " at " + String.format("%.1fm", entity.distance()));
    }

    // 3. Physiological Alerts (Narrative)
    if (status.vitality() < 30) {
      informativeEvents.add("Your body is severely injured.");
    }
    if (status.hunger() > 80) {
      informativeEvents.add("You are starving, you must eat now.");
    }
    if (status.energy() < 15) {
      informativeEvents.add("You are on the verge of collapse due to exhaustion.");
    }
    if (status.stress() > 80) {
      informativeEvents.add("You are at the limit of your mental endurance.");
    }

    return new ReflexResult(false, null, informativeEvents);
  }
}

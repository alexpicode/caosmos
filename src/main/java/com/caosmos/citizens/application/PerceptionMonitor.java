package com.caosmos.citizens.application;

import com.caosmos.citizens.domain.model.perception.CurrentState;
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
  public ReflexResult evaluate(CurrentState currentState, WorldPerception perception) {
    List<String> informativeEvents = new ArrayList<>();

    // 1. Check for zone change reflexes
    String previousZone = currentState.getCurrentZone();
    String newZone = perception.location().zone();

    if (previousZone == null) {
      currentState.setCurrentZone(newZone);
    } else if (!previousZone.equals(newZone)) {
      currentState.setCurrentZone(newZone);
      informativeEvents.add("Has entrado a la zona: " + newZone);
      return new ReflexResult(true, "Llegada/Entrada a zona: " + newZone, informativeEvents);
    }

    // 2. Check for critical threats or immediate social interactions

    for (NearbyEntity entity : perception.nearbyEntities()) {
      // Simulated logic: if entity is very close, it's a potential social or danger event
      if (entity.distance() < 1.5) {
        return new ReflexResult(true, "Entity too close: " + entity.name(), informativeEvents);
      }

      // Log informative events
      informativeEvents.add("Seen " + entity.name() + " at " + String.format("%.1fm", entity.distance()));
    }

    return new ReflexResult(false, null, informativeEvents);
  }
}

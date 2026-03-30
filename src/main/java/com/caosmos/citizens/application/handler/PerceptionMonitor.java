package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Monitors perception to detect critical stimuli that require immediate attention (reflexes). Pure evaluator that
 * doesn't mutate state directly.
 */
@Component
@RequiredArgsConstructor
public class PerceptionMonitor {

  private final TaskRegistry taskRegistry;

  /**
   * Evaluates perception to determine if there's a critical reason to stop the current task. Returns a
   * PerceptionEvaluation containing reflexes and recommended state changes.
   */
  public PerceptionEvaluation evaluate(
      Citizen citizen,
      WorldPerception perception,
      boolean allowsRoutineInterruptions
  ) {
    var currentState = citizen.getCurrentState();
    var status = citizen.getPerception().status();
    List<String> informativeEvents = new ArrayList<>();

    // 1. Check for zone changes
    String previousZoneId = currentState.getCurrentZoneId();
    String newZoneId = perception.location().zoneId();
    String newZoneName = perception.location().zone();

    boolean zoneChanged = !Objects.equals(previousZoneId, newZoneId);
    String pendingZoneId = null;
    String pendingZoneName = null;

    if (zoneChanged) {
      pendingZoneId = newZoneId;
      pendingZoneName = newZoneName;

      // Only handle valid zones (ignore Unknown Territory for reflexes/events)
      if (newZoneId != null) {
        boolean isNewZone = !citizen.isZoneVisited(newZoneId);

        if (allowsRoutineInterruptions) {
          // Check for specific search target in ExploreTask
          String targetFound = checkSearchTarget(citizen.getUuid(), perception.location().tags());

          if (targetFound != null) {
            informativeEvents.add("SEARCH COMPLETE! You've found the " + targetFound + " in " + newZoneName);
            return new PerceptionEvaluation(
                new ReflexResult(true, "Target found: " + targetFound, informativeEvents),
                pendingZoneId, pendingZoneName, true
            );
          }

          if (isNewZone) {
            informativeEvents.add("NOVELTY! You've entered an unexplored zone: " + newZoneName);
            return new PerceptionEvaluation(
                new ReflexResult(true, "Zone discovery: " + newZoneName, informativeEvents),
                pendingZoneId, pendingZoneName, true
            );
          }
        }

        informativeEvents.add("You've entered the zone: " + newZoneName);
      }
    }

    // 2. Check for critical threats or immediate social interactions
    for (NearbyEntity entity : perception.nearbyEntities()) {
      if (entity.distance() < PhysiologicalThresholds.ENTITY_PROXIMITY_ALERT_DISTANCE ||
          hasTag(entity, "hostile")) {
        return new PerceptionEvaluation(
            new ReflexResult(true, "Threat detected: " + entity.name(), informativeEvents),
            pendingZoneId, pendingZoneName, zoneChanged
        );
      }

      if (allowsRoutineInterruptions) {
        if (hasTag(entity, "interesting")) {
          informativeEvents.add("INTERESTING! You've spotted: " + entity.name());
          return new PerceptionEvaluation(
              new ReflexResult(true, "Object of interest: " + entity.name(), informativeEvents),
              pendingZoneId, pendingZoneName, zoneChanged
          );
        }

        if (hasTag(entity, "resource")) {
          informativeEvents.add(
              "You've seen a resource: " + entity.name() + " at " + String.format("%.1fm", entity.distance()));
          return new PerceptionEvaluation(
              new ReflexResult(true, "Resource detected: " + entity.name(), informativeEvents),
              pendingZoneId, pendingZoneName, zoneChanged
          );
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

    return new PerceptionEvaluation(
        new ReflexResult(false, null, informativeEvents),
        pendingZoneId, pendingZoneName, zoneChanged
    );
  }

  private String checkSearchTarget(UUID citizenId, Set<String> currentTags) {
    return taskRegistry.get(citizenId)
        .filter(task -> task instanceof ExploreTask)
        .map(task -> (ExploreTask) task)
        .map(ExploreTask::getTargetTag)
        .filter(target -> target != null && currentTags.stream().anyMatch(target::equalsIgnoreCase))
        .orElse(null);
  }

  private boolean hasTag(NearbyEntity entity, String tag) {
    return entity.tags() != null &&
        entity.tags().stream().anyMatch(tag::equalsIgnoreCase);
  }
}

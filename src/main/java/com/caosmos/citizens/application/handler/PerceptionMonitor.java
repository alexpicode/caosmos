package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.application.social.SocialHeuristicsEngine;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.ReflexResult;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.SpeechTone;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  private final SocialHeuristicsEngine socialHeuristicsEngine;

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

    // --- 0. Prepare Zone Context ---
    String previousZoneId = currentState.getCurrentZoneId();
    String newZoneId = perception.location().zoneId();
    String newZoneName = perception.location().zone();
    boolean zoneChanged = !Objects.equals(previousZoneId, newZoneId);
    String pendingZoneId = zoneChanged ? newZoneId : null;
    String pendingZoneName = zoneChanged ? newZoneName : null;

    // --- 1. Critical Reflexes (Survival First) ---
    String currentTargetId = null;
    if (currentState.getActiveTask() != null) {
      currentTargetId = currentState.getActiveTask().targetId();
    }

    for (NearbyElement element : perception.nearbyElements()) {
      if (EntityType.CITIZEN != element.type() && EntityType.OBJECT != element.type()) {
        continue;
      }

      if (currentTargetId != null && currentTargetId.equals(element.id())) {
        continue; // Ignore the active target for threshold interruptions
      }

      boolean isHostile = hasTag(element, "hostile");
      boolean isCloseObject = EntityType.OBJECT == element.type() &&
          element.distance() < PhysiologicalThresholds.ENTITY_PROXIMITY_ALERT_DISTANCE;

      if (isHostile) {
        return new PerceptionEvaluation(
            new ReflexResult(true, "Threat detected: " + element.name(), informativeEvents),
            pendingZoneId, pendingZoneName, zoneChanged
        );
      }

      if (isCloseObject) {
        informativeEvents.add("Nearby object: " + element.name());
      }
    }

    // --- 1.5 Social Interruptions (Communication) ---
    List<SpeechMessage> messages = perception.nearbyElements().stream()
        .filter(e -> EntityType.SPEECH.equals(e.type()))
        .map(e -> {
          SpeechTone tone = e.tags().isEmpty() ? SpeechTone.NEUTRAL : SpeechTone.fromString(e.tags().iterator().next());
          return new SpeechMessage(e.id(), e.sourceId(), e.name(), e.targetId(), e.message(), tone);
        })
        .toList();

    Optional<SpeechMessage> socialStimulus = socialHeuristicsEngine.evaluate(citizen, messages);
    if (socialStimulus.isPresent()) {
      SpeechMessage trigger = socialStimulus.get();
      informativeEvents.add("SOCIAL INTERRUPTION! " + trigger.sourceName() + " says: " + trigger.message());
      return new PerceptionEvaluation(
          new ReflexResult(true, "Social interruption: " + trigger.sourceName(), informativeEvents),
          pendingZoneId, pendingZoneName, zoneChanged
      );
    }

    // --- 2. Search Target Discovery (Objective Priority) ---
    if (allowsRoutineInterruptions) {
      PerceptionEvaluation searchResult = checkSearchSuccess(
          citizen,
          perception,
          informativeEvents,
          pendingZoneId,
          pendingZoneName,
          zoneChanged
      );
      if (searchResult != null) {
        return searchResult;
      }
    }

    // --- 3. Zone Changes and Progress ---
    if (zoneChanged && newZoneId != null) {
      boolean isNewZone = !citizen.isZoneVisited(newZoneId);
      if (allowsRoutineInterruptions && isNewZone) {
        informativeEvents.add("NOVELTY! You've entered an unexplored zone: " + newZoneName);
        return new PerceptionEvaluation(
            new ReflexResult(true, "Zone discovery: " + newZoneName, informativeEvents),
            pendingZoneId, pendingZoneName, true
        );
      }
      informativeEvents.add("You've entered the zone: " + newZoneName);
    }

    // --- 4. Content Interruptions (Interests/Resources) ---
    if (allowsRoutineInterruptions) {
      for (NearbyElement element : perception.nearbyElements()) {
        if (EntityType.CITIZEN != element.type() && EntityType.OBJECT != element.type()) {
          continue;
        }
        if (hasTag(element, "interesting")) {
          informativeEvents.add("INTERESTING! You've spotted: " + element.name());
        }

        if (hasTag(element, "resource")) {
          informativeEvents.add("You've seen a resource: " + element.name());
        }
      }
    }

    // --- 5. Physiological Alerts (Narrative) ---
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

  private PerceptionEvaluation checkSearchSuccess(
      Citizen citizen,
      WorldPerception perception,
      List<String> informativeEvents,
      String pendingZoneId,
      String pendingZoneName,
      boolean zoneChanged
  ) {
    String targetFound;

    // 1. Current location (only if zone just changed to avoid infinite loops)
    if (zoneChanged) {
      targetFound = checkSearchTarget(citizen.getUuid(), perception.location().category());
      if (targetFound != null) {
        informativeEvents.add(
            "SEARCH COMPLETE! You've found the " + targetFound + " in " + perception.location().zone());
        return new PerceptionEvaluation(
            new ReflexResult(true, "Target found: " + targetFound, informativeEvents),
            pendingZoneId, pendingZoneName, zoneChanged
        );
      }
    }

    // 2. Nearby elements (Entities and Zones)
    for (var element : perception.nearbyElements()) {
      // Skip already-known zones — citizen should use TRAVEL_TO instead of discovering them again
      if (EntityType.ZONE.equals(element.type()) && citizen.isZoneVisited(element.id())) {
        continue;
      }

      targetFound = checkSearchTarget(citizen.getUuid(), element.category());
      if (targetFound != null) {
        String prefix = EntityType.ZONE.equals(element.type()) ? "the " : "";
        informativeEvents.add("SEARCH COMPLETE! You've found " + prefix + targetFound + ": " + element.name());
        return new PerceptionEvaluation(
            new ReflexResult(true, "Target found: " + targetFound, informativeEvents),
            pendingZoneId, pendingZoneName, zoneChanged
        );
      }
    }

    return null;
  }

  private String checkSearchTarget(UUID citizenId, String currentCategory) {
    if (currentCategory == null) {
      return null;
    }
    return taskRegistry.get(citizenId)
        .filter(task -> task instanceof ExploreTask)
        .map(task -> (ExploreTask) task)
        .map(ExploreTask::getTargetCategory)
        .filter(target -> target != null && target.equalsIgnoreCase(currentCategory))
        .orElse(null);
  }

  private boolean hasTag(NearbyElement element, String tag) {
    return element.tags() != null &&
        element.tags().stream().anyMatch(tag::equalsIgnoreCase);
  }
}

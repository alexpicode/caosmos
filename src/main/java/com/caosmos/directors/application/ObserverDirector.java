package com.caosmos.directors.application;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.MutationType;
import com.caosmos.common.domain.model.actions.StateMutation;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.directors.domain.contracts.ObservationProvider;
import com.caosmos.directors.domain.model.ObservationRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the 'Examine' case. It follows a hunt for object data across: 1. World instance (persistence) 2.
 * Templates (JSON definition) 3. AI Directing (Creative fallback)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObserverDirector {

  private final ObservationProvider observationProvider;
  private final WorldPort worldPort;
  private final CitizenPort citizenPort;
  private final EffectResolver effectResolver;

  @Value("${caosmos.directors.observer.style:informative and technical}")
  private String defaultStyle;

  /**
   * Main orchestration for the 'EXAMINE' action.
   */
  public String orchestrateObservation(UUID citizenId, String targetId) {
    // Resolve possession status and physical entity
    boolean isPossessed = citizenPort.isItemEquipped(citizenId, targetId) ||
        citizenPort.isItemInInventory(citizenId, targetId);

    // Initialize object data placeholders
    String description = null;
    Set<String> tags = new HashSet<>();
    String name = targetId;
    String category = "UNKNOWN";
    String context = isPossessed ? "INVENTORY" : "GROUND";
    if (citizenPort.isItemEquipped(citizenId, targetId)) {
      context = "EQUIPPED";
    }

    // Step 1: Resolve data from World Instance
    var targetOpt = worldPort.getObject(targetId);
    if (targetOpt.isPresent()) {
      WorldElement obj = targetOpt.get();
      description = obj.getDescription();
      tags = obj.getTags();
      name = obj.getName();
      category = obj.getCategory();
    } else if (isPossessed) {
      // Fallback: Resolve basic data from inventory/equipment state
      tags = citizenPort.getEquippedItemTags(citizenId, targetId);
    }

    // Step 2: Creative Path (Directed AI Perception)
    if (description == null || description.isBlank()) {
      description = resolveCreativeDescription(citizenId, targetId, name, category, tags, context);
    }

    return description;
  }

  private String resolveCreativeDescription(
      UUID citizenId, String targetId, String name, String category,
      Set<String> tags, String context
  ) {
    String currentZoneId = citizenPort.getCurrentZoneId(citizenId);
    Set<String> envTags = worldPort.getNormalizedEnvironmentTags(currentZoneId)
        .stream()
        .map(EnvironmentImpactTag::name)
        .collect(Collectors.toSet());

    String description = observeViaAi(name, category, tags, envTags, context);

    // Persist the AI veredict so future searches hit Step 1
    StateMutation mutation = new StateMutation(
        targetId,
        MutationType.SET_DESCRIPTION,
        null,
        description
    );
    effectResolver.resolve(citizenId, List.of(mutation));

    return description;
  }

  private String observeViaAi(String name, String category, Set<String> tags, Set<String> envTags, String context) {
    log.debug("ObserverDirector: Generating creative description for {} ({})", name, category);
    ObservationRequest request = new ObservationRequest(name, category, tags, envTags, context, defaultStyle);
    return observationProvider.observe(request);
  }
}

package com.caosmos.directors.application;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.actions.ResolutionResult;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.directors.domain.contracts.ArbitrationProvider;
import com.caosmos.directors.domain.model.ArbitrationRequest;
import com.caosmos.directors.domain.model.CacheKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * High-level orchestrator for 'USE' interactions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorArbitrator {

  private final WisdomCacheService cacheService;
  private final CacheKeyGenerator keyGenerator;
  private final ArbitrationProvider arbitrationProvider;
  private final WorldPort worldPort;
  private final CitizenPort citizenPort;
  private final EffectResolver effectResolver;

  public ActionResult resolveInteraction(UUID citizenId, String targetId, String actionType, String toolRef) {
    // 1. Gather semantic context tags
    Vector3 citizenPosition = citizenPort.getPosition(citizenId);
    Vector3 targetPosition = worldPort.getObjectPosition(targetId).orElse(citizenPosition);

    Set<String> toolTags = toolRef != null ?
        citizenPort.getTagsByToolReference(citizenId, toolRef) :
        Collections.emptySet();

    Set<String> targetTags = worldPort.getObjectTags(targetId);

    String currentZoneId = citizenPort.getCurrentZoneId(citizenId);
    Set<String> envTags = worldPort.getNormalizedEnvironmentTags(currentZoneId).stream()
        .map(EnvironmentImpactTag::name)
        .collect(Collectors.toSet());

    // 2. Build the intent
    ActionIntent intent = new ActionIntent(
        citizenId,
        actionType,
        targetId,
        toolTags,
        targetTags,
        envTags,
        citizenPosition,
        targetPosition
    );

    String targetName = worldPort.getObject(targetId).map(e -> e.getName()).orElse(targetId);
    String targetCategory = worldPort.getObject(targetId).map(e -> e.getCategory()).orElse("object");

    // 3. Resolve (Cache or AI)
    ResolutionResult result = arbitrate(intent, targetName, targetCategory);

    // 4. Transform to ActionResult and apply effects
    if (result.success()) {
      log.info("Action intent: {}, Mutations requested: {}", intent, result.mutations());
      effectResolver.resolve(citizenId, result.mutations());

      Map<String, Object> changes = new HashMap<>();
      changes.put("narration", result.narration());
      return new ActionResult(true, result.narration(), actionType, changes);
    } else {
      return ActionResult.failure(result.narration(), actionType);
    }
  }

  private ResolutionResult arbitrate(ActionIntent intent, String targetName, String targetCategory) {
    // 1. Ensure tags are properly sorted to guarantee deterministic SHA-256 hash generation
    TreeSet<String> toolTags = intent.toolTags() != null ? new TreeSet<>(intent.toolTags()) : new TreeSet<>();
    TreeSet<String> targetTags = intent.targetTags() != null ? new TreeSet<>(intent.targetTags()) : new TreeSet<>();
    TreeSet<String> envTags =
        intent.environmentTags() != null ? new TreeSet<>(intent.environmentTags()) : new TreeSet<>();

    // 2. Generate a unique hash key based on the action semantics
    CacheKey key = keyGenerator.generate(intent.verb(), toolTags, targetTags, envTags);

    // 3. Fast Path: Check if this exact physical interaction was already resolved before
    Optional<ResolutionResult> cachedOptional = cacheService.lookup(key);
    if (cachedOptional.isPresent()) {
      log.debug("[WISDOM CACHE] HIT for key: {}", key.hash());
      return cachedOptional.get();
    }

    // 4. Creative Path (AI Fallback): If it's a new interaction, delegate to the AI Physics Arbitrator
    log.debug("[WISDOM CACHE] MISS for key: {}. Delegating to ArbitrationProvider.", key.hash());
    ArbitrationRequest request = new ArbitrationRequest(
        intent.verb(), toolTags, targetTags, envTags, targetName, targetCategory
    );

    ResolutionResult result = arbitrationProvider.arbitrate(request);

    // 5. Save the AI veredict in the cache so we never have to ask the AI again for the same scenario
    if (result.shouldCache()) {
      cacheService.store(key, result);
    }

    return result;
  }
}

package com.caosmos.directors.application;

import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.actions.ResolutionResult;
import com.caosmos.directors.domain.contracts.ArbitrationProvider;
import com.caosmos.directors.domain.model.ArbitrationRequest;
import com.caosmos.directors.domain.model.CacheKey;
import java.util.Optional;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorArbitrator {

  private final WisdomCacheService cacheService;
  private final CacheKeyGenerator keyGenerator;
  private final ArbitrationProvider arbitrationProvider;

  public ResolutionResult resolve(ActionIntent intent, String targetName, String targetCategory) {
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

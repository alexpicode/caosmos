package com.caosmos.common.infrastructure.manifest;

import com.caosmos.common.domain.contracts.repository.ManifestRepository;
import com.caosmos.common.domain.model.manifest.AgentManifest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Repository for managing in-memory cache of agent manifests.
 */
@Component
@Slf4j
public class ManifestInMemoryRepository implements ManifestRepository {

  private final Map<String, AgentManifest> manifestCache;

  public ManifestInMemoryRepository() {
    this.manifestCache = new ConcurrentHashMap<>();
  }

  public Optional<AgentManifest> get(String manifestName) {
    return Optional.ofNullable(manifestCache.get(manifestName));
  }

  public Optional<AgentManifest> put(String manifestName, AgentManifest manifest) {
    log.debug("[REPOSITORY] Cached manifest: {}", manifestName);
    return Optional.ofNullable(manifestCache.put(manifestName, manifest));
  }

  public Optional<AgentManifest> remove(String manifestName) {
    AgentManifest removed = manifestCache.remove(manifestName);
    if (removed != null) {
      log.debug("[REPOSITORY] Evicted manifest from cache: {}", manifestName);
    }
    return Optional.ofNullable(removed);
  }

  public boolean contains(String manifestName) {
    return manifestCache.containsKey(manifestName);
  }

  public Set<String> getAllManifestNames() {
    return Set.copyOf(manifestCache.keySet());
  }

  public Map<String, AgentManifest> getAllManifests() {
    return Map.copyOf(manifestCache);
  }

  public void clear() {
    int size = manifestCache.size();
    manifestCache.clear();
    log.info("[REPOSITORY] Cleared {} manifests from cache", size);
  }

  public int size() {
    return manifestCache.size();
  }

  public boolean isEmpty() {
    return manifestCache.isEmpty();
  }
}

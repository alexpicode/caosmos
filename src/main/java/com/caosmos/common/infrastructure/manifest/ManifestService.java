package com.caosmos.common.infrastructure.manifest;

import com.caosmos.common.application.manifest.ManifestManager;
import com.caosmos.common.domain.model.manifest.AgentManifest;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrator for agent manifest loading and hot-reload functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManifestService implements ManifestManager {

  private final ManifestParser manifestParser;
  private final ManifestInMemoryRepository manifestInMemoryRepository;
  private final ManifestFileSystemResolver fileSystemResolver;
  private final ManifestWatcher manifestWatcher;

  public void init() {
    loadAllManifests();
    manifestWatcher.startWatching(this::handleManifestChange);
  }

  /**
   * Loads an agent manifest using the overlay system and cache. External manifests take priority over internal ones.
   *
   * @param manifestName Name of the manifest file
   * @return AgentManifest if found, null otherwise
   */
  public AgentManifest loadManifest(String manifestName) {
    // Check cache first
    return manifestInMemoryRepository.get(manifestName)
                                     .orElseGet(() -> loadAndCacheManifest(manifestName));
  }

  public void stopWatching() {
    manifestWatcher.stopWatching();
  }

  private void loadAllManifests() {
    try {
      fileSystemResolver.getAllManifestPaths()
                        .forEach(path -> parseAndCacheManifest(path, path.getFileName().toString()));
    } catch (Exception e) {
      log.error("[MANIFEST] Error loading manifests: {}", e.getMessage());
    }
  }

  private AgentManifest loadAndCacheManifest(String manifestName) {
    // Resolve file path using overlay strategy
    Path manifestPath = fileSystemResolver.findManifestPath(manifestName);
    if (manifestPath == null) {
      log.warn("[MANIFEST] Manifest not found: {}", manifestName);
      return null;
    }

    return parseAndCacheManifest(manifestPath, manifestName);
  }

  private AgentManifest parseAndCacheManifest(Path manifestPath, String manifestName) {
    try {
      AgentManifest manifest = manifestParser.parseManifest(manifestPath);
      manifestInMemoryRepository.put(manifestName, manifest);
      log.info("[MANIFEST] Loaded manifest: {}", manifestName);
      return manifest;
    } catch (IOException e) {
      log.error("[MANIFEST] Error parsing manifest {}: {}", manifestName, e.getMessage());
      return null;
    }
  }

  private void handleManifestChange(String manifestName) {
    log.info("[MANIFEST] Hot-reloading manifest: {}", manifestName);

    // Remove from cache to force reload
    manifestInMemoryRepository.remove(manifestName);

    // Reload the manifest
    loadManifest(manifestName);
  }
}

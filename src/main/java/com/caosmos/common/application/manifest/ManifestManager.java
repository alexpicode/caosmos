package com.caosmos.common.application.manifest;

import com.caosmos.common.domain.model.manifest.AgentManifest;

/**
 * Interface for managing agent manifest loading and hot-reload functionality. Defines the contract for manifest
 * operations including loading, caching, and watching.
 */
public interface ManifestManager {

  /**
   * Initializes the manifest manager by loading all manifests and starting watch service.
   */
  void init();

  /**
   * Loads an agent manifest using the overlay system and cache. External manifests take priority over internal ones.
   *
   * @param manifestName Name of the manifest file
   * @return AgentManifest if found, null otherwise
   */
  AgentManifest loadManifest(String manifestName);

  /**
   * Stops the manifest watching service.
   */
  void stopWatching();
}

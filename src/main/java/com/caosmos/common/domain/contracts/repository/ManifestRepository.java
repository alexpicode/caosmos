package com.caosmos.common.domain.contracts.repository;

import com.caosmos.common.domain.model.manifest.AgentManifest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for managing agent manifests repository. Defines the contract for manifest storage and retrieval
 * operations.
 */
public interface ManifestRepository {

  Optional<AgentManifest> get(String manifestName);

  Optional<AgentManifest> put(String manifestName, AgentManifest manifest);

  Optional<AgentManifest> remove(String manifestName);

  boolean contains(String manifestName);

  Set<String> getAllManifestNames();

  Map<String, AgentManifest> getAllManifests();

  void clear();

  int size();

  boolean isEmpty();
}

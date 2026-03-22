package com.caosmos.common.application.agents;

/**
 * Interface for managing population operations. Defines the contract for spawning and managing agents in the system.
 */
public interface PopulationService {

  /**
   * Spawns all agents from available manifests. Initializes the global spawn sequence for all registered agents
   * manifests.
   */
  void spawnAll();

  /**
   * Spawns a specific agent from a given manifest.
   *
   * @param manifestName the name of the manifest to spawn the agent from
   * @param agentId      the unique identifier for the new agent
   */
  void spawnCitizen(String manifestName, String agentId);
}

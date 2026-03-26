package com.caosmos.citizens.application;

import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.common.application.agents.LifeManager;
import com.caosmos.common.application.agents.PopulationService;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.repository.ManifestRepository;
import com.caosmos.common.domain.model.manifest.AgentManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenPopulationService implements PopulationService {

  @Value("classpath:/prompts/citizen-system.md")
  private Resource systemPromptResource;

  @Value("classpath:/prompts/citizen-user.md")
  private Resource userPromptResource;

  @Value("${caosmos.citizen.pulse-frequency}")
  private int pulseFrequencyRealSeconds;

  @Value("${caosmos.world.time.speed:10.0}")
  private double simulationSpeed;

  private final ManifestRepository manifestRepository;
  private final LifeManager lifeManager;
  private final CitizenRegistry citizenRegistry;
  private final ObjectMapper objectMapper;
  private final CitizenTaskManager taskManager;
  private final CitizenDecisionMaker decisionMaker;
  private final CitizenPerceptionHandler perceptionHandler;
  private final PhysiologicalMotor physiologicalMotor;
  private final EntityTelemetryService telemetryService;
  private final CitizenSettings citizenSettings;

  public void spawnAll() {
    log.info("[POPULATION] Initializing global spawn sequence...");

    manifestRepository.getAllManifestNames().forEach(name -> {
      try {
        UUID citizenId = UUID.randomUUID();
        spawnCitizen(name, citizenId.toString());
      } catch (Exception e) {
        log.error("[POPULATION] Failed to spawn citizen from {}: {}", name, e.getMessage());
      }
    });

    log.info("[POPULATION] Global spawn complete. Total citizens: {}", manifestRepository.size());
  }

  @Override
  public void spawnCitizen(String manifestName, String citizenId) {
    log.info("[POPULATION] Spawning citizen {} from manifest {}", citizenId, manifestName);

    AgentManifest manifest = manifestRepository.get(manifestName)
                                               .orElseThrow(() -> new RuntimeException(
                                                   "Manifest not found: " + manifestName));

    Map<String, Object> combinedData = new HashMap<>(manifest.metadata());
    combinedData.put("personality", manifest.personality());
    combinedData.put("manifestId", manifestName);

    CitizenProfile citizenProfile = objectMapper.convertValue(combinedData, CitizenProfile.class);

    Citizen citizen = new Citizen(UUID.fromString(citizenId), citizenProfile);

    citizenRegistry.register(UUID.fromString(citizenId), citizen);

    // Orchestrator that handles the cognitive cycle
    int pulseFrequencySimulatedSeconds = (int) (pulseFrequencyRealSeconds * simulationSpeed);

    PulseConfiguration configuration = new PulseConfiguration(
        pulseFrequencySimulatedSeconds,
        systemPromptResource,
        userPromptResource,
        citizenSettings.getMaxTicksWithoutDecision()
    );

    CitizenPulse citizenPulse = new CitizenPulse(
        citizen,
        taskManager,
        decisionMaker,
        perceptionHandler,
        physiologicalMotor,
        configuration,
        telemetryService
    );

    lifeManager.startLife(citizenId, citizenPulse);
  }
}
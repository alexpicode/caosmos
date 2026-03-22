package com.caosmos.citizens.application;

import com.caosmos.citizens.application.model.FullPerception;
import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.application.model.PulseContext;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.common.application.telemetry.BiometricsEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.AgentPulse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the cognitive cycle of a citizen using specialized service components. Acts as the main coordinator
 * between task management, decision making, and perception handling.
 */
@Slf4j
@RequiredArgsConstructor
public class CitizenPulse implements AgentPulse {

  private final Citizen citizen;
  private final CitizenTaskManager taskManager;
  private final CitizenDecisionMaker decisionMaker;
  private final CitizenPerceptionHandler perceptionHandler;
  private final PulseConfiguration pulseConfiguration;
  private final EntityTelemetryService telemetryService;

  private final List<String> incrementalEvents = new ArrayList<>();

  @Override
  public void pulse(long tick) {
    String citizenName = citizen.getCitizenProfile().identity().name();
    log.info("[CITIZEN:{}] Pulsing at tick: {}", citizenName, tick);

    // 1. Update domain state (decay vitality)
    citizen.decayVitality(pulseConfiguration.vitalityDecayAmount());

    // 1.5 Register Biometrics
    telemetryService.registerBiometrics(new BiometricsEntry(
        citizen.getUuid(),
        tick,
        citizen.getPerception().status().vitality(),
        citizen.getPerception().status().energy()
    ));

    // 2. Execute Active Task
    taskManager.executeActiveTask(citizen);

    // 3. Handle Perception & Create Context
    var fullPerception = perceptionHandler.handlePerception(citizen, incrementalEvents);

    PulseContext context = new PulseContext(
        citizenName,
        tick,
        fullPerception,
        new ArrayList<>(incrementalEvents),
        citizen.getLastAction()
    );

    // 4. Check for critical interruptions
    if (fullPerception.reflex().critical() && citizen.getLastAction() != null) {
      handleCriticalInterruption(context, fullPerception);
      return;
    }

    // 5. Decision Phase (Ask LLM for a new goal if idle)
    if (CitizenState.IDLE.equals(citizen.getState())) {
      log.info("[CITIZEN:{}] Entering Decision Phase...", citizenName);
      performDecision(context);
    }
  }

  private void handleCriticalInterruption(PulseContext context, FullPerception fullPerception) {
    String citizenName = citizen.getCitizenProfile().identity().name();
    String originalIntent = citizen.getActiveTask() != null ? citizen.getActiveTask().goal() : "None";

    log.info("[CITIZEN:{}] CRITICAL INTERRUPTION: {}", citizenName, fullPerception.reflex().reason());

    // Cancel current task and set state to thinking/interrupted
    String reason = fullPerception.reflex().reason();
    String detailedReason = String.format("Stopped because: %s (Original intent: %s)", reason, originalIntent);

    // Create an INTERRUPTED LastAction
    LastAction interruptedAction = citizen.getLastAction().withStatus("INTERRUPTED");

    // Cancel current task and set state functionally
    taskManager.cancelActiveTask(citizen, CitizenState.INTERRUPTED, interruptedAction);

    // Force direct decision (status message is now in citizen)
    performDecision(context);

  }


  private void performDecision(PulseContext context) {
    // Delegate to decision maker
    var lastAction = decisionMaker.makeDecision(citizen, context, pulseConfiguration);

    // Update state with decision results
    citizen.transitionTo(CitizenState.IDLE, lastAction);

    // Clear incremental events after they've been processed in a decision
    incrementalEvents.clear();
  }


}

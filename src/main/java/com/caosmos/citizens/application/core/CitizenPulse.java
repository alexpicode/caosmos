package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.handler.CitizenPerceptionHandler;
import com.caosmos.citizens.application.model.PhysiologicalReflex;
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
import java.util.Map;
import java.util.Optional;
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
  private final PhysiologicalMotor physiologicalMotor;
  private final PulseConfiguration pulseConfiguration;
  private final EntityTelemetryService telemetryService;

  private final List<String> unprocessedEvents = new ArrayList<>();


  @Override
  public void pulse(long tick) {
    String citizenName = citizen.getCitizenProfile().identity().name();
    log.info("[CITIZEN:{}] Pulsing at tick: {}", citizenName, tick);

    // 1. Update passive domain state (decay metabolism)
    double dt = pulseConfiguration.pulseFrequencySeconds();
    physiologicalMotor.applyPassiveMetabolism(citizen, dt);

    // 1.5 Register Biometrics
    telemetryService.registerBiometrics(new BiometricsEntry(
        citizen.getUuid(),
        tick,
        citizen.getPerception().status().vitality(),
        citizen.getPerception().status().energy()
    ));

    // 2. Check Physiological Reflexes (Higher Priority Safety Layer)
    Optional<PhysiologicalReflex> physReflex = physiologicalMotor.evaluateCriticalThresholds(citizen);
    physReflex.ifPresent(r -> {
      r.events().forEach(e -> {
        if (!unprocessedEvents.contains(e)) {
          unprocessedEvents.add(e);
        }
      });
    });

    if (physReflex.isPresent() && physReflex.get().critical()) {
      PulseContext context = new PulseContext(
          citizenName,
          tick,
          null,
          new ArrayList<>(unprocessedEvents),
          citizen.getLastAction()
      );
      handleInterruption(
          physReflex.get().forcedActionType(),
          physReflex.get().reason(),
          physReflex.get().events(),
          context,
          true,
          "CRITICAL_INTERRUPT"
      );
      return;
    }

    // 3. Execute Active Task
    taskManager.executeActiveTask(citizen);

    // 4. Handle Perception & Create Context
    boolean allowsRoutine = false;
    if (citizen.getActiveTask() != null) {
      allowsRoutine = citizen.getActiveTask().allowsRoutineInterruptions();
    }

    var fullPerception = perceptionHandler.handlePerception(citizen, unprocessedEvents, allowsRoutine);

    PulseContext context = new PulseContext(
        citizenName,
        tick,
        fullPerception,
        new ArrayList<>(unprocessedEvents),
        citizen.getLastAction()
    );

    // 5. Check Perception-based interruptions
    if (fullPerception.reflex().critical() && citizen.getLastAction() != null) {
      String interruptReason =
          allowsRoutine && !fullPerception.reflex().reason().contains("Threat") ? "ROUTINE_INTERRUPT"
              : "CRITICAL_INTERRUPT";
      handleInterruption(
          citizen.getLastAction().type(),
          fullPerception.reflex().reason(),
          new ArrayList<>(),
          context,
          true,
          interruptReason
      );
      return;
    }

    // 6. Decision Phase
    if (CitizenState.IDLE.equals(citizen.getState())) {
      log.info("[CITIZEN:{}] Entering Decision Phase (IDLE)...", citizenName);
      performDecision(context);
    }
  }

  private void handleInterruption(
      String actionType, String reason, List<String> events, PulseContext context,
      boolean cancelTask, String interruptType
  ) {
    String citizenName = citizen.getCitizenProfile().identity().name();
    log.info("[CITIZEN:{}] INTERRUPTION: {} (Action: {}, CancelTask: {})", citizenName, reason, actionType, cancelTask);

    // Ensure events are added to the list for the perception context
    events.forEach(e -> {
      if (!unprocessedEvents.contains(e)) {
        unprocessedEvents.add(e);
      }
    });

    // Preserve existing action parameters while updating status and result
    LastAction original = citizen.getLastAction();
    if (original == null) {
      original = new LastAction(
          actionType != null ? actionType : "UNKNOWN",
          interruptType,
          reason,
          reason,
          Map.of()
      );
    }

    LastAction interruptedAction = original
        .withStatus(interruptType)
        .withResultMessage(reason + (events.isEmpty() ? "" : " | " + String.join(" | ", events)));

    if (actionType != null) {
      interruptedAction = interruptedAction.withType(actionType);
    }

    // Cancel current task and set state to thinking/interrupted if requested
    if (cancelTask) {
      taskManager.cancelActiveTask(citizen, CitizenState.INTERRUPTED, interruptedAction);
    } else {
      // Just transition to state to signal thinking, but keep the task
      citizen.transitionTo(CitizenState.INTERRUPTED, interruptedAction);
    }

    // Force direct decision
    performDecision(context);
  }

  private void performDecision(PulseContext context) {
    // Delegate to decision maker
    var lastAction = decisionMaker.makeDecision(citizen, context, pulseConfiguration);

    // Update state with decision results
    citizen.transitionTo(CitizenState.IDLE, lastAction);

    // Clear unprocessed events after they've been processed in a decision
    unprocessedEvents.clear();
  }


}

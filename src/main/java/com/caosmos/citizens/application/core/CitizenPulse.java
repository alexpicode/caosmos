package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.handler.CitizenPerceptionHandler;
import com.caosmos.citizens.application.model.PhysiologicalReflex;
import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.application.model.PulseContext;
import com.caosmos.citizens.application.social.ConversationManager;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.InterruptType;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.citizens.domain.model.social.ConversationPhase;
import com.caosmos.common.application.telemetry.BiometricsEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.AgentPulse;
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
  private final ConversationManager conversationManager;

  private final EventBuffer eventBuffer = new EventBuffer();

  @Override
  public void pulse(long tick) {
    String citizenName = citizen.getCitizenProfile().identity().name();
    log.debug("[CITIZEN:{}] Pulsing at tick: {}", citizenName, tick);

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
    physReflex.ifPresent(r -> eventBuffer.addAll(r.events()));

    if (physReflex.isPresent() && physReflex.get().critical()) {
      // Collect latest perception for the decision context
      var fullPerception = perceptionHandler.handlePerception(citizen, eventBuffer.snapshot(), false);
      citizen.updateRecentMessages(fullPerception.recentMessages());
      handleInterruption(
          tick,
          citizenName,
          physReflex.get().forcedActionType(),
          physReflex.get().reason(),
          physReflex.get().events(),
          true,
          InterruptType.CRITICAL,
          fullPerception
      );
      return;
    }

    // 3. Handle Perception & Create Context
    boolean allowsRoutine = false;
    if (citizen.getActiveTask() != null) {
      allowsRoutine = citizen.getActiveTask().allowsRoutineInterruptions();
    }

    var fullPerception = perceptionHandler.handlePerception(citizen, eventBuffer.snapshot(), allowsRoutine);
    citizen.updateRecentMessages(fullPerception.recentMessages());

    // 4. Check Perception-based interruptions
    if (fullPerception.reflex().critical() && citizen.getLastAction() != null) {
      InterruptType type = allowsRoutine && !fullPerception.reflex().reason().contains("Threat")
          ? InterruptType.ROUTINE
          : InterruptType.CRITICAL;

      handleInterruption(
          tick,
          citizenName,
          citizen.getLastAction().type(),
          fullPerception.reflex().reason(),
          List.of(), // Events already in buffer
          true,
          type,
          fullPerception
      );
      return;
    }

    // 5. Execute Active Task
    taskManager.executeActiveTask(citizen, fullPerception);

    // 5.5 Sync Conversation State
    boolean isTalking = isCitizenTalking();
    if (isTalking && CitizenState.IDLE.equals(citizen.getState())) {
      citizen.transitionTo(CitizenState.TALKING, "Engaged in conversation");
    } else if (!isTalking && CitizenState.TALKING.equals(citizen.getState())) {
      citizen.transitionTo(CitizenState.IDLE, "Conversation ended or stale");
    }

    // 6. Decision Phase
    if (shouldCitizenThink()) {
      log.debug("[CITIZEN:{}] Entering Decision Phase ({})...", citizenName, citizen.getState());
      performDecision(createContext(tick, citizenName, fullPerception));
    }
  }

  private void handleInterruption(
      long tick, String citizenName, String actionType, String reason, List<String> events,
      boolean cancelTask, InterruptType interruptType, FullPerception fullPerception
  ) {
    log.debug(
        "[CITIZEN:{}] INTERRUPTION: {} (Action: {}, Type: {}, CancelTask: {})",
        citizenName, reason, actionType, interruptType, cancelTask
    );

    eventBuffer.addAll(events);

    LastAction interruptedAction = buildInterruptedAction(actionType, reason, interruptType);

    if (cancelTask) {
      taskManager.cancelActiveTask(citizen, CitizenState.INTERRUPTED, interruptedAction);
    } else {
      citizen.transitionTo(CitizenState.INTERRUPTED, interruptedAction);
    }

    performDecision(createContext(tick, citizenName, fullPerception));
  }

  private LastAction buildInterruptedAction(String actionType, String reason, InterruptType type) {
    LastAction original = citizen.getLastAction();
    if (original == null) {
      original = new LastAction(
          actionType != null ? actionType : "UNKNOWN",
          type.name(),
          reason,
          reason,
          Map.of()
      );
    }

    LastAction interrupted = original.withStatus(type.name())
        .withResultMessage(reason);

    if (actionType != null) {
      interrupted = interrupted.withType(actionType);
    }
    return interrupted;
  }

  private PulseContext createContext(long tick, String citizenName, FullPerception fullPerception) {
    // 1. Refresh the citizen's own domain state perception.
    // This is vital because executeActiveTask (e.g. EQUIP) might have modified the domain state
    // since the initial pulse perception was captured.
    FullPerception refreshedPerception = new FullPerception(
        citizen.getPerception(),
        fullPerception.world(),
        fullPerception.reflex(),
        fullPerception.recentMessages()
    );

    return new PulseContext(
        citizenName,
        tick,
        refreshedPerception,
        eventBuffer.snapshot(),
        citizen.getLastAction()
    );
  }

  private void performDecision(PulseContext context) {
    var lastAction = decisionMaker.makeDecision(citizen, context, pulseConfiguration);
    CitizenState nextState = isCitizenTalking() ? CitizenState.TALKING : CitizenState.IDLE;
    citizen.transitionTo(nextState, lastAction);
    eventBuffer.clear();
  }

  private boolean isCitizenTalking() {
    var activeSession = conversationManager.getActiveSession(citizen.getUuid().toString());
    if (activeSession.isPresent()) {
      var phase = activeSession.get().getPhase();
      return phase == ConversationPhase.ACTIVE || phase == ConversationPhase.INITIATED;
    }
    return false;
  }

  private boolean shouldCitizenThink() {
    CitizenState state = citizen.getState();

    // Always think if IDLE or INTERRUPTED (standard behavior)
    if (CitizenState.IDLE.equals(state) || CitizenState.INTERRUPTED.equals(state)) {
      return true;
    }

    // If TALKING, only think if it's our turn or the session has been lost
    if (CitizenState.TALKING.equals(state)) {
      var sessionOpt = conversationManager.getActiveSession(citizen.getUuid().toString());
      if (sessionOpt.isPresent()) {
        var session = sessionOpt.get();

        // 1. It's our turn to respond (last speaker was someone else)
        if (!citizen.getUuid().toString().equals(session.getLastSpeakerId())) {
          return true;
        }

        // 2. We are waiting for others to speak
        return false;
      }
      // If we are in TALKING state but no session exists, we should think to exit this state
      return true;
    }

    return false;
  }
}


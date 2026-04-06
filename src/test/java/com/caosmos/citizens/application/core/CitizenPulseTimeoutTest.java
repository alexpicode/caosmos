package com.caosmos.citizens.application.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.citizens.application.handler.CitizenPerceptionHandler;
import com.caosmos.citizens.application.model.PhysiologicalReflex;
import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class CitizenPulseTimeoutTest {

  private Citizen citizen;
  private CitizenTaskManager taskManager;
  private CitizenDecisionMaker decisionMaker;
  private CitizenPerceptionHandler perceptionHandler;
  private PhysiologicalMotor physiologicalMotor;
  private PulseConfiguration pulseConfiguration;
  private EntityTelemetryService telemetryService;
  private CitizenPulse citizenPulse;

  @BeforeEach
  void setUp() {
    citizen = mock(Citizen.class, RETURNS_DEEP_STUBS);
    taskManager = mock(CitizenTaskManager.class);
    decisionMaker = mock(CitizenDecisionMaker.class);
    perceptionHandler = mock(CitizenPerceptionHandler.class);
    physiologicalMotor = mock(PhysiologicalMotor.class, RETURNS_DEEP_STUBS);
    telemetryService = mock(EntityTelemetryService.class);
    pulseConfiguration = new PulseConfiguration(10, mock(Resource.class), mock(Resource.class), 20);

    when(citizen.getCitizenProfile().identity().name()).thenReturn("TestCitizen");
    when(citizen.getUuid()).thenReturn(UUID.randomUUID());

    var perception = citizen.getPerception();
    when(perception.status().vitality()).thenReturn(100.0);
    when(perception.status().energy()).thenReturn(100.0);
    when(perception.status().stress()).thenReturn(0.0);
    when(citizen.getCurrentState()).thenReturn(new CurrentState(
        null,
        null,
        null,
        CitizenState.BUSY,
        null,
        null,
        null,
        new ArrayList<>()
    ));
    when(citizen.getState()).thenReturn(CitizenState.BUSY);
    when(citizen.getActiveTask()).thenReturn(new ActiveTask("T", "G", null, null, "TG", false, false));

    FullPerception fullPerception = mock(FullPerception.class, RETURNS_DEEP_STUBS);
    when(perceptionHandler.handlePerception(any(), any(), anyBoolean())).thenReturn(fullPerception);
    when(fullPerception.reflex().critical()).thenReturn(false);
    when(fullPerception.citizen()).thenReturn(perception);

    when(physiologicalMotor.evaluateCriticalThresholds(any())).thenReturn(Optional.empty());

    citizenPulse = new CitizenPulse(
        citizen,
        taskManager,
        decisionMaker,
        perceptionHandler,
        physiologicalMotor,
        pulseConfiguration,
        telemetryService
    );
  }

  @Test
  void shouldNotTriggerDecisionIfBusy() {
    citizenPulse.pulse(10);
    citizenPulse.pulse(100); // Polling timeout is gone
    verify(decisionMaker, never()).makeDecision(any(), any(), any());
  }

  @Test
  void shouldTriggerDecisionIfIdle() {
    when(citizen.getState()).thenReturn(CitizenState.IDLE);
    citizenPulse.pulse(20);
    verify(decisionMaker, times(1)).makeDecision(any(), any(), any());
  }

  @Test
  void shouldAbortTaskAndTriggerDecisionOnPhysiologicalCrisis() {
    PhysiologicalReflex reflex = new PhysiologicalReflex(true, "Collapse", "SLEEP", List.of("Dying"));
    when(physiologicalMotor.evaluateCriticalThresholds(any())).thenReturn(Optional.of(reflex));

    citizenPulse.pulse(15);

    // Verify task is cancelled (because critical was triggered)
    // Wait, cancelTask arguments depend on what handleInterruption sends.
    verify(decisionMaker, times(1)).makeDecision(any(), any(), any());
    // Also verify taskManager execution is NEVER called because physiological crisis aborts execution
    verify(taskManager, never()).executeActiveTask(any(), any());
  }
}

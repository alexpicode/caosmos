package com.caosmos.citizens.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.citizens.application.model.FullPerception;
import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
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
    when(citizen.getCurrentState()).thenReturn(new CurrentState(null, null, null, CitizenState.BUSY, null, null));

    FullPerception fullPerception = mock(FullPerception.class, RETURNS_DEEP_STUBS);
    when(perceptionHandler.handlePerception(any(), any())).thenReturn(fullPerception);
    when(fullPerception.reflex().critical()).thenReturn(false);
    when(fullPerception.citizen()).thenReturn(perception);

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
  void shouldNotTriggerDecisionIfTimeoutNotReached() {
    citizenPulse.pulse(10);
    verify(decisionMaker, never()).makeDecision(any(), any(), any());
  }

  @Test
  void shouldTriggerDecisionIfTimeoutReached() {
    citizenPulse.pulse(20);
    // handleInterruption -> performDecision
    verify(decisionMaker, times(1)).makeDecision(any(), any(), any());
  }

  @Test
  void shouldNotTriggerTimeoutIfStateIsIdle() {
    // Manually set state to IDLE for this test
    when(citizen.getState()).thenReturn(CitizenState.IDLE);

    citizenPulse.pulse(25);

    // It should call it once because it's IDLE, but the internal lastDecisionTick will be updated
    verify(decisionMaker, times(1)).makeDecision(any(), any(), any());
  }

  @Test
  void shouldTriggerDecisionButNotCancelTaskOnTimeout() {
    citizenPulse.pulse(20);

    // Verify decision was triggered
    verify(decisionMaker, times(1)).makeDecision(any(), any(), any());

    // Verify task was NOT cancelled
    verify(taskManager, never()).cancelActiveTask(any(Citizen.class), any(CitizenState.class), any(String.class));
    verify(taskManager, never()).cancelActiveTask(any(Citizen.class), any(CitizenState.class), any(LastAction.class));
  }
}

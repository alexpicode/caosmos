package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversationTask implements Task {

  private final String targetId;
  private final Vector3 targetPosition;
  private final Supplier<Boolean> isSessionEnded;

  public ConversationTask(String targetId, Vector3 targetPosition, Supplier<Boolean> isSessionEnded) {
    this.targetId = targetId;
    this.targetPosition = targetPosition;
    this.isSessionEnded = isSessionEnded;
  }

  @Override
  public CitizenState getCitizenState() {
    return CitizenState.BUSY;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    double hours = dt / 3600.0;
    citizen.biology().decreaseStress(PhysiologicalThresholds.SOCIAL_STRESS_REDUCTION_RATE * hours);

    // Maintain orientation towards target
    log.trace("Citizen {} in conversation with {} at {}", citizen.getUuid(), targetId, targetPosition);

    if (isSessionEnded.get()) {
      log.info("Conversation with {} ended or went stale for citizen {}", targetId, citizen.getUuid());
      return toActiveTask(citizen).withCompleted(true).withGoal("Conversation ended");
    }

    return toActiveTask(citizen);
  }

  @Override
  public ActiveTask toActiveTask(Citizen citizen) {
    return new ActiveTask(
        "CONVERSATION",
        "Talking with " + targetId,
        targetId,
        "SOCIAL",
        "Social interaction",
        false,
        false
    );
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true;
  }
}

package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.common.domain.model.world.Vector3;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConversationTask implements Task {

  private final String targetId;
  private final Vector3 targetPosition;
  private int ticksRemaining = 10; // Default timeout if no response

  public ConversationTask(String targetId, Vector3 targetPosition) {
    this.targetId = targetId;
    this.targetPosition = targetPosition;
  }

  @Override
  public CitizenState getCitizenState() {
    return CitizenState.BUSY;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    ticksRemaining--;

    // Maintain orientation towards target
    log.trace("Citizen {} in conversation with {} at {}", citizen.getUuid(), targetId, targetPosition);

    if (ticksRemaining <= 0) {
      log.info("Conversation with {} timed out for citizen {}", targetId, citizen.getUuid());
      return toActiveTask(citizen).withCompleted(true).withGoal("Conversation timeout");
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
    return false;
  }
}

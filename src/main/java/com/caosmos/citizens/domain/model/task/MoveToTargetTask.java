package com.caosmos.citizens.domain.model.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.common.domain.model.world.Vector3;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that moves a citizen towards a specific target position.
 */
@Slf4j
public class MoveToTargetTask implements Task {

  private static final double ARRIVAL_THRESHOLD = 0.2;

  private final Vector3 target;
  private final String targetId;

  public MoveToTargetTask(Vector3 target, String targetId) {
    this.target = target;
    this.targetId = targetId;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    Vector3 currentPos = citizen.getCurrentState().getPosition();
    double distance = currentPos.distanceTo(target);

    if (distance <= ARRIVAL_THRESHOLD) {
      log.info("Citizen {} reached target {}", citizen.getUuid(), target);
      citizen.getCurrentState().setPosition(target); // Snap to target
      return new ActiveTask("MoveToTarget", "Reached target", targetId, true);
    }

    // Calculate movement distance for this tick
    double moveDistance = walkingSpeed * dt;

    // Prevent overshooting
    if (moveDistance > distance) {
      moveDistance = distance;
    }

    // Calculate new position
    double ratio = moveDistance / distance;
    double newX = currentPos.x() + (target.x() - currentPos.x()) * ratio;
    double newY = currentPos.y() + (target.y() - currentPos.y()) * ratio;
    double newZ = currentPos.z() + (target.z() - currentPos.z()) * ratio;

    Vector3 newPos = new Vector3(newX, newY, newZ);
    citizen.getCurrentState().setPosition(newPos);

    log.debug("Citizen {} moved towards {}. New pos: {}", citizen.getUuid(), target, newPos);

    boolean isComplete = moveDistance >= distance; // Complete if we reached or overshot
    return new ActiveTask("MoveToTarget", "Moving to target", targetId, isComplete);
  }

}

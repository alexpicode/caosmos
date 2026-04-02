package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.common.domain.model.world.Vector3;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that moves a citizen towards a specific target position. High focus navigation.
 */
@Slf4j
public class TravelToTask implements Task {

  private static final double ARRIVAL_THRESHOLD = 0.2;

  private final Vector3 target;
  private final String targetId;

  public TravelToTask(Vector3 target, String targetId) {
    this.target = target;
    this.targetId = targetId;
  }

  @Override
  public CitizenState getCitizenState() {
    return CitizenState.MOVING;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    Vector3 currentPos = citizen.getCurrentState().getPosition();
    double distance = currentPos.distanceTo(target);
    var biology = citizen.biology();
    double hours = dt / 3600.0;

    if (distance <= ARRIVAL_THRESHOLD) {
      log.info("Citizen {} reached target {}", citizen.getUuid(), target);
      citizen.getCurrentState().setPosition(target); // Snap to target
      return new ActiveTask("TravelTo", "Reached target", targetId, true, allowsRoutineInterruptions());
    }

    // --- Physiological Costs ---
    // Energy decay
    biology.decreaseEnergy(PhysiologicalThresholds.MOVE_ENERGY_COST_RATE * hours);
    // Hunger increase
    biology.increaseHunger(PhysiologicalThresholds.MOVE_HUNGER_COST_RATE * hours);

    // Calculate movement distance for this tick
    double currentWalkingSpeed = walkingSpeed;
    if (biology.getEnergy() < PhysiologicalThresholds.ENERGY_EXTREME_FATIGUE) {
      currentWalkingSpeed *= PhysiologicalThresholds.EXTREME_FATIGUE_SPEED_FACTOR;
    }

    double moveDistance = currentWalkingSpeed * dt;

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
    return new ActiveTask("TravelTo", "Traveling to target", targetId, isComplete, allowsRoutineInterruptions());
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return false;
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Travel to {} interrupted. Reason: {}", target, reason);
  }

}

package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.world.CollisionResult;
import com.caosmos.common.domain.model.world.Vector3;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Task for exploring the environment in a specific direction. Low focus, stops on novelty.
 */
@Slf4j
public class ExploreTask implements Task {

  private static final double EXPLORATION_LIMIT = 50.0;
  private static final double ARRIVAL_THRESHOLD = 0.5;

  private final Vector3 directionNormalized;
  @Getter
  private final String targetCategory;
  private final String reason;
  private final WorldPort worldPort;
  private Vector3 startPosition;
  private Vector3 targetPosition;

  public ExploreTask(Vector3 direction, String targetCategory, String reason, WorldPort worldPort) {
    this.directionNormalized = direction.normalize();
    this.targetCategory = targetCategory != null ? targetCategory.toLowerCase() : null;
    this.reason = reason;
    this.worldPort = worldPort;
  }

  @Override
  public CitizenState getCitizenState() {
    return CitizenState.MOVING;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    Vector3 currentPos = citizen.getCurrentState().getPosition();
    var biology = citizen.biology();
    double hours = dt / 3600.0;

    // Initialize positions on first tick
    if (startPosition == null) {
      startPosition = currentPos;
      targetPosition = new Vector3(
          startPosition.x() + directionNormalized.x() * EXPLORATION_LIMIT,
          startPosition.y() + directionNormalized.y() * EXPLORATION_LIMIT,
          startPosition.z() + directionNormalized.z() * EXPLORATION_LIMIT
      );
      log.info("Citizen {} started exploration from {} towards {}", citizen.getUuid(), startPosition, targetPosition);
    }

    double distanceToTarget = currentPos.distanceTo(targetPosition);
    double distanceTraveled = currentPos.distanceTo(startPosition);

    if (distanceTraveled >= EXPLORATION_LIMIT || distanceToTarget <= ARRIVAL_THRESHOLD) {
      log.info("Citizen {} completed exploration distance limit.", citizen.getUuid());
      return toActiveTask(citizen).withCompleted(true).withGoal("Exploration reached limit");
    }

    // --- Physiological Costs ---
    // Slightly higher costs during active exploration
    biology.decreaseEnergy(PhysiologicalThresholds.MOVE_ENERGY_COST_RATE * 1.2 * hours);
    biology.increaseHunger(PhysiologicalThresholds.MOVE_HUNGER_COST_RATE * 1.2 * hours);

    // Calculate movement
    double moveDistance = walkingSpeed * dt;
    if (moveDistance > distanceToTarget) {
      moveDistance = distanceToTarget;
    }

    double ratio = moveDistance / distanceToTarget;
    double newX = currentPos.x() + (targetPosition.x() - currentPos.x()) * ratio;
    double newY = currentPos.y() + (targetPosition.y() - currentPos.y()) * ratio;
    double newZ = currentPos.z() + (targetPosition.z() - currentPos.z()) * ratio;

    Vector3 newPos = new Vector3(newX, newY, newZ);

    // Validate collision
    String currentZoneId = citizen.getCurrentState().getCurrentZoneId();
    CollisionResult collision = worldPort.validateMovement(currentPos, newPos, currentZoneId);

    citizen.getCurrentState().setPosition(collision.clampedPosition());

    if (collision.wasBlocked()) {
      log.info("Citizen {} exploration blocked by interior boundary.", citizen.getUuid());
      return toActiveTask(citizen).withCompleted(true).withGoal("Blocked by wall");
    }

    return toActiveTask(citizen);
  }

  @Override
  public ActiveTask toActiveTask(Citizen citizen) {
    return new ActiveTask(
        "EXPLORE",
        "Exploring environment",
        null,
        targetCategory,
        reason,
        false,
        allowsRoutineInterruptions()
    );
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true;
  }

  @Override
  public void onInterrupt(String reason) {
    log.debug("Exploration interrupted. Reason: {}", reason);
  }
}

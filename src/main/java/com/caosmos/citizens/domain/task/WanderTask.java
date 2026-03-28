package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Task for wandering around aimlessly. Low focus.
 */
@Slf4j
public class WanderTask implements Task {

  private double elapsedSeconds = 0;

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    elapsedSeconds += dt;

    // Passive decay while wandering
    citizen.consumeEnergy(PhysiologicalThresholds.WAIT_ENERGY_DECAY_RATE * 2 * (dt / 3600.0));
    citizen.increaseHunger(PhysiologicalThresholds.PASSIVE_HUNGER_RATE * 2 * (dt / 3600.0));

    boolean isComplete = (elapsedSeconds / 3600.0) >= 0.5; // Wander for 30 mins
    return new ActiveTask("WANDER", "Wandering aimlessly", null, isComplete, allowsRoutineInterruptions());
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true;
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Wandering interrupted. Reason: {}", reason);
  }
}

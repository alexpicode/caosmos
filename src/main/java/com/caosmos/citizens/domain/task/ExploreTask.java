package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Task for exploring the environment. Low focus.
 */
@Slf4j
public class ExploreTask implements Task {

  private double elapsedSeconds = 0;

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    elapsedSeconds += dt;

    // Slightly higher costs during active exploration
    citizen.consumeEnergy(PhysiologicalThresholds.PASSIVE_ENERGY_DECAY_RATE * 1.5 * (dt / 3600.0));
    citizen.increaseHunger(PhysiologicalThresholds.PASSIVE_HUNGER_RATE * 1.5 * (dt / 3600.0));

    boolean isComplete = (elapsedSeconds / 3600.0) >= 1.0; // Explore for 1 hour
    return new ActiveTask("EXPLORE", "Exploring interest points", null, isComplete, allowsRoutineInterruptions());
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true;
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Exploration interrupted. Reason: {}", reason);
  }
}

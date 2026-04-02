package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that represents resting/relaxing. Modestly recovers energy and reduces stress.
 */
@Slf4j
public class RestTask implements Task {

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    var biology = citizen.biology();
    double hours = dt / 3600.0;

    // 1. Recover Energy
    biology.increaseEnergy(PhysiologicalThresholds.REST_ENERGY_RECOVERY_RATE * hours);

    // 2. Reduce Stress
    biology.decreaseStress(PhysiologicalThresholds.REST_STRESS_REDUCTION_RATE * hours);

    boolean isComplete = biology.getEnergy() >= 100.0;

    return new ActiveTask("REST", "Resting and relaxing", null, isComplete, allowsRoutineInterruptions());
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true; // Low focus state allows for routine interruptions
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Resting interrupted. Reason: {}", reason);
  }
}

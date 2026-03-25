package com.caosmos.citizens.domain.model.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that represents resting/relaxing. Modestly recovers energy and reduces stress.
 */
@Slf4j
public class RestTask implements Task {

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    // 1. Recover Energy
    citizen.increaseEnergy(PhysiologicalThresholds.REST_ENERGY_RECOVERY_RATE * (dt / 3600.0));

    // 2. Reduce Stress
    citizen.reduceStress(PhysiologicalThresholds.REST_STRESS_REDUCTION_RATE * (dt / 3600.0));

    boolean isComplete = citizen.getPerception().status().energy() >= 100.0;

    return new ActiveTask("REST", "Resting and relaxing", null, isComplete);
  }
}

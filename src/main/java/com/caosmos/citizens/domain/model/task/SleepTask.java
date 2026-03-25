package com.caosmos.citizens.domain.model.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that represents sleeping. Recovers energy, reduces stress and restores vitality.
 */
@Slf4j
public class SleepTask implements Task {

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    Status status = citizen.getPerception().status();

    // 1. Recover Energy
    citizen.increaseEnergy(PhysiologicalThresholds.SLEEP_ENERGY_RECOVERY_RATE * (dt / 3600.0));

    // 2. Reduce Stress
    citizen.reduceStress(PhysiologicalThresholds.SLEEP_STRESS_REDUCTION_RATE * (dt / 3600.0));

    // 3. Increment Hunger (slightly)
    citizen.increaseHunger(PhysiologicalThresholds.SLEEP_HUNGER_INCREASE_RATE * (dt / 3600.0));

    // 4. Restore Vitality (if not starving)
    if (status.hunger() < PhysiologicalThresholds.HUNGER_CRISIS) {
      citizen.increaseVitality(PhysiologicalThresholds.SLEEP_VITALITY_RESTORATION_RATE * (dt / 3600.0));
    }

    boolean isComplete = citizen.getPerception().status().energy() >= 100.0;
    if (isComplete) {
      log.info("Citizen {} is fully rested.", citizen.getUuid());
    }

    return new ActiveTask("SLEEP", "Sleeping and recovering", null, isComplete);
  }
}

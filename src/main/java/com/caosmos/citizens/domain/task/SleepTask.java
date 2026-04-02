package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that represents sleeping. Recovers energy, reduces stress and restores vitality.
 */
@Slf4j
public class SleepTask implements Task {

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    var status = citizen.getPerception().status();
    var biology = citizen.biology();
    double hours = dt / 3600.0;

    // 1. Recover Energy
    biology.increaseEnergy(PhysiologicalThresholds.SLEEP_ENERGY_RECOVERY_RATE * hours);

    // 2. Reduce Stress
    biology.decreaseStress(PhysiologicalThresholds.SLEEP_STRESS_REDUCTION_RATE * hours);

    // 3. Increment Hunger (slightly)
    biology.increaseHunger(PhysiologicalThresholds.SLEEP_HUNGER_INCREASE_RATE * hours);

    // 4. Restore Vitality (if not starving)
    if (status.hunger() < PhysiologicalThresholds.HUNGER_CRISIS) {
      biology.increaseVitality(PhysiologicalThresholds.SLEEP_VITALITY_RESTORATION_RATE * hours);
    }

    boolean isComplete = biology.getEnergy() >= 100.0;
    if (isComplete) {
      log.info("Citizen {} is fully rested.", citizen.getUuid());
    }

    return new ActiveTask("SLEEP", "Sleeping and recovering", null, isComplete, allowsRoutineInterruptions());
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Sleep interrupted. Waking up because: {}", reason);
  }
}

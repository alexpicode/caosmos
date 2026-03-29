package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import lombok.extern.slf4j.Slf4j;

/**
 * Task for working at a specific workplace.
 */
@Slf4j
public class WorkTask implements Task {

  private final String workplaceType;
  private double elapsedSeconds = 0;

  public WorkTask(String workplaceType) {
    this.workplaceType = workplaceType;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    elapsedSeconds += dt;
    double hours = dt / 3600.0;
    var biology = citizen.biology();

    double energyRate;
    double hungerRate;
    double stressRate;

    if ("mine".equalsIgnoreCase(workplaceType)) {
      energyRate = PhysiologicalThresholds.MINE_ENERGY_DRAIN_RATE;
      hungerRate = PhysiologicalThresholds.MINE_HUNGER_INCREASE_RATE;
      stressRate = PhysiologicalThresholds.MINE_STRESS_INCREASE_RATE;
    } else {
      // Default / "shop"
      energyRate = PhysiologicalThresholds.SHOP_ENERGY_DRAIN_RATE;
      hungerRate = PhysiologicalThresholds.SHOP_HUNGER_INCREASE_RATE;
      stressRate = PhysiologicalThresholds.SHOP_STRESS_INCREASE_RATE;
    }

    biology.decreaseEnergy(Math.abs(energyRate) * hours);
    biology.increaseHunger(hungerRate * hours);
    biology.increaseStress(stressRate * hours);

    boolean shiftComplete = (elapsedSeconds / 3600.0) >= PhysiologicalThresholds.DEFAULT_WORK_DURATION_HOURS;

    return new ActiveTask(
        "WORK",
        "Working in " + workplaceType,
        workplaceType,
        shiftComplete,
        allowsRoutineInterruptions()
    );
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Leaving work in {} because: {}", workplaceType, reason);
  }
}

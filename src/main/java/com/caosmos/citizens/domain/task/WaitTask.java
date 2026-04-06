package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import lombok.extern.slf4j.Slf4j;

/**
 * Task that represents waiting or active repose. Reduces stress if in a safe zone.
 */
@Slf4j
public class WaitTask implements Task {

  private final boolean inSafeZone;
  private double elapsedSeconds = 0;

  public WaitTask(boolean inSafeZone) {
    this.inSafeZone = inSafeZone;
  }

  @Override
  public ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed) {
    elapsedSeconds += dt;
    double hours = dt / 3600.0;
    var biology = citizen.biology();

    // 1. Minimum energy decay (active repose)
    biology.decreaseEnergy(Math.abs(PhysiologicalThresholds.WAIT_ENERGY_DECAY_RATE) * hours);

    // 2. Stress reduction in safe zone
    if (inSafeZone) {
      biology.decreaseStress(Math.abs(PhysiologicalThresholds.SAFE_ZONE_STRESS_REDUCTION_RATE) * hours);
    }

    boolean isComplete = (elapsedSeconds / 60.0) >= PhysiologicalThresholds.DEFAULT_WAIT_DURATION_MINUTES;
    return toActiveTask(citizen).withCompleted(isComplete);
  }

  @Override
  public ActiveTask toActiveTask(Citizen citizen) {
    String statusDetails = inSafeZone ? "In Safe Zone" : "In Danger Zone";
    return new ActiveTask(
        "WAIT",
        "Waiting/Standing by",
        null,
        null,
        statusDetails,
        false,
        allowsRoutineInterruptions()
    );
  }

  @Override
  public boolean allowsRoutineInterruptions() {
    return true; // Low focus state allows for routine interruptions
  }

  @Override
  public void onInterrupt(String reason) {
    log.info("Wait interrupted. Reason: {}", reason);
  }
}

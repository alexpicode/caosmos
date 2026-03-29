package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
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
  public ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed) {
    elapsedSeconds += dt;
    double hours = dt / 3600.0;
    var biology = citizen.biology();

    // 1. Minimum energy decay (active repose)
    biology.decreaseEnergy(Math.abs(PhysiologicalThresholds.WAIT_ENERGY_DECAY_RATE) * hours);

    // 2. Stress reduction in safe zone
    if (inSafeZone) {
      biology.decreaseStress(Math.abs(PhysiologicalThresholds.SAFE_ZONE_STRESS_REDUCTION_RATE) * hours);
    }

    boolean isComplete = (elapsedSeconds / 3600.0) >= PhysiologicalThresholds.DEFAULT_WAIT_DURATION_HOURS;
    return new ActiveTask("WAIT", "Waiting in repose", null, isComplete, allowsRoutineInterruptions());
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

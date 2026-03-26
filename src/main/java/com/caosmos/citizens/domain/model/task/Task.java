package com.caosmos.citizens.domain.model.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;

/**
 * Interface for long-running tasks executed by a citizen.
 */
public interface Task {

  /**
   * Returns the semantic state of the citizen while performing this task.
   */
  default CitizenState getCitizenState() {
    return CitizenState.BUSY;
  }

  /**
   * Executes one step of the task.
   *
   * @param citizen      The citizen performing the task.
   * @param dt           The time elapsed since the last execution (in seconds).
   * @param walkingSpeed The citizen's walking speed (in m/s).
   * @return ActiveTask with current task status.
   */
  ActiveTask executeOnTick(Citizen citizen, double dt, double walkingSpeed);
}

package com.caosmos.citizens.domain.task;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;

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
   * @param perception   The current field of view and status of the citizen.
   * @param dt           The time elapsed since the last execution (in seconds).
   * @param walkingSpeed The citizen's walking speed (in m/s).
   * @return ActiveTask with current task status.
   */
  ActiveTask executeOnTick(Citizen citizen, FullPerception perception, double dt, double walkingSpeed);

  /**
   * Called when the task is forcibly interrupted by the system (e.g. physiological crisis or danger).
   */
  default void onInterrupt(String reason) {
    // Optional cleanup
  }

  /**
   * Indicates if the citizen's attention is low enough to be interrupted by routine events (e.g. noticing resources,
   * crossing zones).
   */
  default boolean allowsRoutineInterruptions() {
    return false;
  }
}

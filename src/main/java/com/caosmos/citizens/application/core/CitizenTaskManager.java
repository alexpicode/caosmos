package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.handler.CitizenPerceptionHandler;
import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.FullPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.citizens.domain.task.Task;
import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages active tasks for citizens during their cognitive cycle. Handles task execution, completion, and
 * cancellation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenTaskManager {

  private final CitizenSettings citizenSettings;
  private final TaskRegistry taskRegistry;
  private final WorldRegistry spatialRegistry;
  private final CitizenPerceptionHandler perceptionHandler;

  public void executeActiveTask(Citizen citizen, FullPerception perception, double dt) {
    Optional<Task> task = taskRegistry.get(citizen.getUuid());

    if (task.isPresent()) {
      String citizenName = citizen.getCitizenProfile().identity().name();

      // Set state according to task definition (only if different)
      CitizenState newState = task.get().getCitizenState();
      if (!newState.equals(citizen.getState())) {
        citizen.transitionTo(newState, "Executing active task: " + task.get().getClass().getSimpleName());
      }

      Vector3 initialPosition = citizen.getPosition();

      // Execute the task
      ActiveTask activeTask = task.get()
          .executeOnTick(citizen, perception, dt, citizenSettings.getWalkingSpeed());

      Vector3 newPosition = citizen.getPosition();
      if (!initialPosition.equals(newPosition)) {
        spatialRegistry.updatePosition(citizen, newPosition);

        // Immediate Zone and MentalMap update for real-time monitoring
        perceptionHandler.synchronizeSpatialContext(citizen, newPosition);
      }

      citizen.updateTask(activeTask);

      if (activeTask.completed()) {
        log.debug("[CITIZEN:{}] Task completed: {}", citizenName, activeTask.goal());
        LastAction completionAction = citizen.getLastAction().withStatus("SUCCESS");
        cancelActiveTask(citizen, CitizenState.IDLE, completionAction);
      } else {
        log.debug("[CITIZEN:{}] Continuing task: {}", citizenName, activeTask);
      }
    }
  }

  /**
   * Cancels the active task for a citizen and sets their state.
   */
  public void cancelActiveTask(Citizen citizen, CitizenState newState, String reason) {
    taskRegistry.get(citizen.getUuid()).ifPresent(task -> task.onInterrupt(reason));
    taskRegistry.remove(citizen.getUuid());
    citizen.clearTask(newState, reason);
  }

  public void cancelActiveTask(Citizen citizen, CitizenState newState, LastAction action) {
    taskRegistry.get(citizen.getUuid()).ifPresent(task -> task.onInterrupt(action.resultMessage()));
    taskRegistry.remove(citizen.getUuid());
    citizen.clearTask(newState, action);
  }


}

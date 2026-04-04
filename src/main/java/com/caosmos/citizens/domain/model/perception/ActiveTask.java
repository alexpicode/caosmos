package com.caosmos.citizens.domain.model.perception;

public record ActiveTask(
    String type,
    String goal,
    String targetId,
    String targetName,
    String targetDescription,
    boolean completed,
    boolean allowsRoutineInterruptions
) {

  public ActiveTask withCompleted(boolean completed) {
    return new ActiveTask(type, goal, targetId, targetName, targetDescription, completed, allowsRoutineInterruptions);
  }

  public ActiveTask withGoal(String goal) {
    return new ActiveTask(type, goal, targetId, targetName, targetDescription, completed, allowsRoutineInterruptions);
  }
}

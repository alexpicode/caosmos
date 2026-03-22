package com.caosmos.citizens.domain.model.perception;

import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.common.domain.model.world.Vector3;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentState {

  private Vector3 position;
  private String currentZone;
  private CitizenState state;
  private ActiveTask activeTask;
  private LastAction lastAction;
}



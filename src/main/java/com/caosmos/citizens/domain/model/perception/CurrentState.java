package com.caosmos.citizens.domain.model.perception;

import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentState {

  private Vector3 position;
  private String currentZone;
  private String currentZoneId;
  private CitizenState state;
  private ActiveTask activeTask;
  private LastAction lastAction;
  private MentalMap mentalMap;
  private List<SpeechMessage> recentMessages = new ArrayList<>();
}



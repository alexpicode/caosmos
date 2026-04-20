package com.caosmos.citizens.application.dto;

import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import java.util.List;

public record CitizenDetailDto(
    CitizenConfigDto config,
    CitizenPerception perception,
    LastAction currentAction,
    String currentZone,
    List<ZoneExplorationDto> explorationProgress,
    double coins,
    List<SpeechMessageDto> recentMessages
) {

  public record ZoneExplorationDto(
      String name,
      int percentage
  ) {

  }
}

package com.caosmos.citizens.application.dto;

import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Detailed representation of a citizen")
public record CitizenDetailDto(
    @Schema(description = "Citizen configuration profile") CitizenConfigDto config,
    @Schema(description = "Citizen's current perception of the world") CitizenPerception perception,
    @Schema(description = "Last action executed by the citizen") LastAction currentAction,
    @Schema(description = "ID of the zone the citizen is currently in") String currentZone,
    @Schema(description = "Exploration progress for different zones") List<ZoneExplorationDto> explorationProgress,
    @Schema(description = "Current coins balance") double coins,
    @Schema(description = "Recent speech messages") List<SpeechMessageDto> recentMessages
) {

  @Schema(description = "Exploration progress of a specific zone")
  public record ZoneExplorationDto(
      @Schema(description = "Name of the zone") String name,
      @Schema(description = "Exploration percentage (0-100)") int percentage
  ) {

  }
}

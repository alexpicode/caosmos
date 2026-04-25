package com.caosmos.citizens.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Represents a step in the citizen's thought process")
public record CitizenCognitionDto(
    @Schema(description = "Unique identifier of the citizen") UUID citizenId,
    @Schema(description = "Simulation tick when this thought occurred") long tick,
    @Schema(description = "Detailed reasoning and internal thoughts") String reasoning,
    @Schema(description = "Target of the intended action (if any)") String actionTarget
) {

}

package com.caosmos.citizens.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Summary representation of a citizen")
public record CitizenSummaryDto(
    @Schema(description = "Unique identifier of the citizen") UUID uuid,
    @Schema(description = "Name of the citizen") String name,
    @Schema(description = "X coordinate") double x,
    @Schema(description = "Y coordinate") double y,
    @Schema(description = "Z coordinate") double z,
    @Schema(description = "Current operational state of the citizen") String state,
    @Schema(description = "Current active goal, if any") String currentGoal,
    @Schema(description = "Current vitality level") double vitality
) {

}

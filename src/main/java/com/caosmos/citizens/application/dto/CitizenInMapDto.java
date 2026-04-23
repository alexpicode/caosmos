package com.caosmos.citizens.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Map representation of a citizen")
public record CitizenInMapDto(
    @Schema(description = "Unique identifier of the citizen") UUID uuid,
    @Schema(description = "X coordinate") double x,
    @Schema(description = "Z coordinate") double z,
    @Schema(description = "Current operational state of the citizen") String state
) {

}

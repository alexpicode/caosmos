package com.caosmos.world.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Information about a specific world spatial chunk")
public record ChunkInfoDto(
    @Schema(description = "Grid X coordinate of the chunk") int gridX,
    @Schema(description = "Grid Z coordinate of the chunk") int gridZ,
    @Schema(description = "Physical size of the chunk side") double size,
    @Schema(description = "Number of entities currently in this chunk") int entityCount,
    @Schema(description = "Current movement cost multiplier for this chunk") double movementCost
) {

}

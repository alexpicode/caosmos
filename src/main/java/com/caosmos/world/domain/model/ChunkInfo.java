package com.caosmos.world.domain.model;

public record ChunkInfo(
    int gridX,
    int gridZ,
    double size,
    int entityCount,
    double movementCost
) {

}

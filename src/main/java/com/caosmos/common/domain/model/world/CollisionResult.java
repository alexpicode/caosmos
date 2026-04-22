package com.caosmos.common.domain.model.world;

/**
 * Represents the result of a collision validation.
 *
 * @param clampedPosition The final position after applying collision constraints.
 * @param wasBlocked      Indicates if the original movement was interrupted by an obstacle.
 */
public record CollisionResult(
    Vector3 clampedPosition,
    boolean wasBlocked
) {

}

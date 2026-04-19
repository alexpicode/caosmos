package com.caosmos.citizens.domain.model.perception;

/**
 * Exploration state of a zone.
 */
public record ExplorationStatus(
    int percentage,
    boolean fullyExplored
) {

}

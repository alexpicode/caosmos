package com.caosmos.citizens.domain.model.perception;

/**
 * Compact summary of a known zone for historical persistence.
 */
public record ZoneMemorySummary(
    String zoneId,
    String name,
    String zoneType,
    String category,
    int explorationPercentage,
    boolean fullyExplored
) {

}

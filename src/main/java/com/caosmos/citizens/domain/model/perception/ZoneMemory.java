package com.caosmos.citizens.domain.model.perception;

import java.util.List;

/**
 * Detailed memory of the citizen's current zone.
 */
public record ZoneMemory(
    String zoneId,
    String name,
    String zoneType,
    // "INTERIOR" / "EXTERIOR"
    String category,
    ExplorationStatus exploration,
    List<RememberedPOI> rememberedPOIs
) {

}

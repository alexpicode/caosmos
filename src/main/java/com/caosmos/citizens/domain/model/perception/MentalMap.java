package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record MentalMap(
    CognitiveAnchor home,
    CognitiveAnchor nearestCity,
    ZoneMemory currentZoneMemory,
    // Detail of the zone where the citizen is now
    List<ZoneMemorySummary> knownZones
    // Compact history of visited zones
) {

}

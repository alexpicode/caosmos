package com.caosmos.common.domain.model.world;

import java.util.Set;

public record NearbyElement(
    String id,
    String name,
    String category,
    EntityType type,
    ZoneType zoneType,
    double distance,
    String direction,
    Set<String> tags,
    String zoneId,
    String sourceId,
    String targetId,
    String message
) {

}

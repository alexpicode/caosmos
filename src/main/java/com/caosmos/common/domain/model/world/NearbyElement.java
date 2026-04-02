package com.caosmos.common.domain.model.world;

import java.util.Set;

public record NearbyElement(
    String id,
    String name,
    String category,
    String type,
    // "OBJECT", "ZONE"
    String zoneType,
    // "INTERIOR", "EXTERIOR" (null for objects)
    double distance,
    String direction,
    Set<String> tags
) {

}

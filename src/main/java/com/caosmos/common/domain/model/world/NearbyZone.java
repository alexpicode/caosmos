package com.caosmos.common.domain.model.world;

import java.util.Set;

public record NearbyZone(
    String id,
    String name,
    String type,
    String category,
    double distance,
    String direction,
    Set<String> tags
) {

}

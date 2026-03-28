package com.caosmos.common.domain.model.world;

import java.util.Set;

public record NearbyZone(
    String id,
    String name,
    String type,
    double distance,
    String direction,
    Set<String> tags
) {

}

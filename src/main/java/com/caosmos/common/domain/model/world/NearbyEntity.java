package com.caosmos.common.domain.model.world;

import java.util.Set;

public record NearbyEntity(
    String id,
    String name,
    double distance,
    String direction,
    Set<String> tags
) {

}
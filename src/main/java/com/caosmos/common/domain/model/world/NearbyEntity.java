package com.caosmos.common.domain.model.world;

import java.util.List;

public record NearbyEntity(
    String id,
    String name,
    double distance,
    String direction,
    List<String> tags
) {

}
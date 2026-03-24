package com.caosmos.common.domain.model.world;

import java.util.Set;

public record Location(
    String zone,
    String type,
    String place,
    Set<String> tags,
    String parentZone,
    String zoneId
) {

}
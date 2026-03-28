package com.caosmos.common.domain.model.world;

import java.util.List;

public record WorldPerception(
    WorldDate date,
    Location location,
    Environment environment,
    List<NearbyEntity> nearbyEntities,
    List<NearbyZone> nearbyZones
) {

}
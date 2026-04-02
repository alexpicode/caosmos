package com.caosmos.common.domain.model.world;

import java.util.List;
import java.util.Set;

public record WorldPerception(
    WorldDate date,
    Location location,
    Environment environment,
    List<NearbyElement> nearbyElements,
    Set<String> categoriesForExplore
) {

}
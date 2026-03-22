package com.caosmos.common.domain.model.world;

import java.util.List;

public record Environment(
    String terrainType,
    List<String> tags,
    String lightLevel
) {

}

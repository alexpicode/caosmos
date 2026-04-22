package com.caosmos.world.application.config;

import com.caosmos.world.domain.model.Zone;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ZonesConfig(
    @JsonProperty("zones") List<Zone> zones
) {

}

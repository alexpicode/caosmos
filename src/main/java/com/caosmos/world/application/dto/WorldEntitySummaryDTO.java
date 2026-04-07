package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.EntityType;

public record WorldEntitySummaryDTO(
    String id,
    EntityType type,
    String displayName,
    double x,
    double y,
    double z
) {

}

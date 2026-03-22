package com.caosmos.world.application.dto;

import java.util.Map;

public record WorldEntitySummaryDTO(
    String id,
    String type,
    double x,
    double y,
    double z,
    String displayName,
    Map<String, Object> properties
) {

}

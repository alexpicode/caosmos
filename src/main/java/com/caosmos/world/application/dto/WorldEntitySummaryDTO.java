package com.caosmos.world.application.dto;

public record WorldEntitySummaryDTO(
    String id,
    String type,
    String displayName,
    double x,
    double y,
    double z
) {

}

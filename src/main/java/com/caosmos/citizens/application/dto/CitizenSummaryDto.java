package com.caosmos.citizens.application.dto;

import java.util.UUID;

public record CitizenSummaryDto(
    UUID uuid,
    String name,
    double x,
    double y,
    double z,
    String state,
    String currentGoal,
    int vitality
) {

}

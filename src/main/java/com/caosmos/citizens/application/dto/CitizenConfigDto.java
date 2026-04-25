package com.caosmos.citizens.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Citizen configuration properties")
public record CitizenConfigDto(
    @Schema(description = "Base walking speed of the citizen") double walkingSpeed
) {

}

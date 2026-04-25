package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.WorldDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current world environment and date status")
public record WorldEnvironmentResponseDto(
    @Schema(description = "Current simulation date and time") WorldDate date,
    @Schema(description = "Current environmental conditions (weather, light)") Environment environment
) {

}

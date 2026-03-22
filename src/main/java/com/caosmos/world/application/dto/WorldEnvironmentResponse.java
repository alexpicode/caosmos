package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.WorldDate;

public record WorldEnvironmentResponse(
    WorldDate date,
    Environment environment
) {

}

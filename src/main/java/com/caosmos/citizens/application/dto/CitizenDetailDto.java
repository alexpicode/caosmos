package com.caosmos.citizens.application.dto;

import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.common.application.telemetry.BiometricsEntry;
import java.util.Collection;

public record CitizenDetailDto(
    CitizenPerception perception,
    LastAction currentAction,
    Collection<BiometricsEntry> biometrics,
    String manifest_id
) {

}

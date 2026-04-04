package com.caosmos.citizens.application.dto;

import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import java.util.Set;

public record CitizenDetailDto(
    CitizenConfigDto config,
    CitizenPerception perception,
    LastAction currentAction,
    String currentZone,
    Set<String> visitedZoneIds
) {

}

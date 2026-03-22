package com.caosmos.common.application.telemetry;

import java.util.UUID;

public record CognitionEntry(
    UUID entityId,
    long tick,
    String thoughtProcess,
    String actionTarget,
    String perceptionSnapshot
) {

}

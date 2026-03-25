package com.caosmos.common.application.telemetry;

import java.util.UUID;

public record BiometricsEntry(
    UUID entityId,
    long tick,
    double vitality,
    double energy
) {

}

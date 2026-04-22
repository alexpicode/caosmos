package com.caosmos.common.domain.model.world;

/**
 * Metadata of a zone for inter-module exchange.
 */
public record ZoneMetadata(
    String zoneId,
    String name,
    String zoneType,
    // "INTERIOR" / "EXTERIOR"
    String category,
    double width,
    double length
) {

}

package com.caosmos.common.domain.model.world;

/**
 * A simple DTO to represent a named entity in the world with a position.
 */
public record NamedLocation(
    String name,
    Vector3 center
) {

}

package com.caosmos.citizens.domain.model.perception;

import java.util.Set;

/**
 * Represents a Point of Interest remembered by a citizen in a zone.
 */
public record RememberedPOI(
    String id,
    String name,
    String category,
    Set<String> tags,
    String relativeDirection
) {

}

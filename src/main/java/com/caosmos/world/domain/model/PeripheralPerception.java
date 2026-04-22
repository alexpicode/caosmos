package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.NearbyElement;
import java.util.List;

/**
 * Result of a unified spatial perception query.
 */
public record PeripheralPerception(
    List<NearbyElement> elements
) {

}

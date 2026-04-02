package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.world.SpatialNarrativePort;
import com.caosmos.common.domain.model.world.NamedLocation;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.service.DirectionCalculator;
import com.caosmos.world.domain.service.SemanticDistanceMapper;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing the SpatialNarrativePort by delegating to specialized world services.
 */
@Component
@RequiredArgsConstructor
public class SpatialNarrativeAdapter implements SpatialNarrativePort {

  private final ZoneManager zoneManager;
  private final DirectionCalculator directionCalculator;
  private final SemanticDistanceMapper distanceMapper;

  @Override
  public String getCardinalDirection(Vector3 from, Vector3 to) {
    return directionCalculator.getCardinalDirection(from, to);
  }

  @Override
  public String getSemanticDistance(double distance) {
    return distanceMapper.mapDistance(distance);
  }

  @Override
  public Optional<NamedLocation> findNearestCity(Vector3 position) {
    return zoneManager.findNearestCity(position)
        .map(zone -> new NamedLocation(zone.getName(), zone.getCenter()));
  }
}

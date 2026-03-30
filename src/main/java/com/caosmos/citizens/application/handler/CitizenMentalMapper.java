package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.CognitiveAnchor;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.service.DirectionCalculator;
import com.caosmos.world.domain.service.NearbyZoneService;
import com.caosmos.world.domain.service.SemanticDistanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Specialized component for calculating a citizen's mental map. Translates physical world data (coordinates, zones)
 * into narrative cognitive anchors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenMentalMapper {

  private final NearbyZoneService nearbyZoneService;
  private final DirectionCalculator directionCalculator;
  private final SemanticDistanceMapper distanceMapper;

  /**
   * Calculates the mental map for a citizen based on their current position and profile.
   */
  public MentalMap calculate(Citizen citizen, Vector3 currentPosition) {
    // 1. Home Anchor
    CognitiveAnchor homeAnchor = null;
    if (citizen.getCitizenProfile().baseLocation() != null) {
      Vector3 homePos = new Vector3(
          citizen.getCitizenProfile().baseLocation().x(),
          citizen.getCitizenProfile().baseLocation().y(),
          citizen.getCitizenProfile().baseLocation().z()
      );
      double distance = currentPosition.distanceTo2D(homePos);
      homeAnchor = new CognitiveAnchor(
          "Home",
          Math.round(distance * 100.0) / 100.0,
          distanceMapper.mapDistance(distance),
          directionCalculator.getCardinalDirection(currentPosition, homePos)
      );
    }

    // 2. Nearest City Anchor
    CognitiveAnchor cityAnchor = null;
    var nearestCityOpt = nearbyZoneService.findNearestCity(currentPosition);
    if (nearestCityOpt.isPresent()) {
      var city = nearestCityOpt.get();
      double distance = currentPosition.distanceTo2D(city.getCenter());
      cityAnchor = new CognitiveAnchor(
          city.getName(),
          Math.round(distance * 100.0) / 100.0,
          distanceMapper.mapDistance(distance),
          directionCalculator.getCardinalDirection(currentPosition, city.getCenter())
      );
    }

    return new MentalMap(homeAnchor, cityAnchor);
  }
}

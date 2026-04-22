package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DirectionCalculator {

  private static final Map<Double, String> DIRECTION_MAP = new HashMap<>();

  static {
    DIRECTION_MAP.put(0.0, "East");
    DIRECTION_MAP.put(45.0, "Northeast");
    DIRECTION_MAP.put(90.0, "North");
    DIRECTION_MAP.put(135.0, "Northwest");
    DIRECTION_MAP.put(180.0, "West");
    DIRECTION_MAP.put(225.0, "Southwest");
    DIRECTION_MAP.put(270.0, "South");
    DIRECTION_MAP.put(315.0, "Southeast");
  }

  public String getCardinalDirection(Vector3 from, Vector3 to) {
    double angle = from.getAngleTo(to);
    double degrees = Math.toDegrees(angle);

    if (degrees < 0) {
      degrees += 360;
    }

    return getDirectionFromDegrees(degrees);
  }

  private String getDirectionFromDegrees(double degrees) {
    if (degrees >= 337.5 || degrees < 22.5) {
      return "East";
    }
    if (degrees >= 22.5 && degrees < 67.5) {
      return "Northeast";
    }
    if (degrees >= 67.5 && degrees < 112.5) {
      return "North";
    }
    if (degrees >= 112.5 && degrees < 157.5) {
      return "Northwest";
    }
    if (degrees >= 157.5 && degrees < 202.5) {
      return "West";
    }
    if (degrees >= 202.5 && degrees < 247.5) {
      return "Southwest";
    }
    if (degrees >= 247.5 && degrees < 292.5) {
      return "South";
    }
    return "Southeast";
  }
}

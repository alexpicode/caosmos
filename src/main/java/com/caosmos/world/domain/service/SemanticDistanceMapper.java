package com.caosmos.world.domain.service;

import org.springframework.stereotype.Service;

@Service
public class SemanticDistanceMapper {

  /**
   * Maps a physical distance in meters to a semantic narrative label.
   */
  public String mapDistance(double distance) {
    if (distance <= 50) {
      return "Immediate";
    }
    if (distance <= 200) {
      return "Near";
    }
    if (distance <= 800) {
      return "Distant";
    }
    if (distance <= 2000) {
      return "Remote";
    }
    return "Unreachable";
  }

}

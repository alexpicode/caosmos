package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ZoneManager {

  private final List<Zone> zones = new ArrayList<>();

  public void addZone(Zone zone) {
    zones.add(zone);
  }

  public Optional<Zone> findZoneAt(Vector3 position) {
    Zone smallestZone = null;
    double smallestArea = Double.MAX_VALUE;

    for (Zone zone : zones) {
      if (zone.contains(position)) {
        double area = zone.getWidth() * zone.getLength();
        if (smallestZone == null || area < smallestArea) {
          smallestZone = zone;
          smallestArea = area;
        }
      }
    }
    return Optional.ofNullable(smallestZone);
  }

  public String getZoneName(Vector3 position) {
    return findZoneAt(position).map(Zone::getName).orElse("Unknown Territory");
  }

  public List<Zone> getAllZones() {
    return new ArrayList<>(zones);
  }

  public void clearZones() {
    zones.clear();
  }
}

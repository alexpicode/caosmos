package com.caosmos.world.application.usecases;

import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldZonesUseCase {

  private final ZoneManager zoneManager;

  public List<Zone> execute() {
    return zoneManager.getAllZones();
  }
}

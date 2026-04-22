package com.caosmos.world.application;

import com.caosmos.common.domain.contracts.JsonLoader;
import com.caosmos.world.application.config.ZonesConfig;
import com.caosmos.world.domain.service.ZoneManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneInitializer {

  private final ZoneManager zoneManager;
  private final JsonLoader jsonLoader;

  @Value("classpath:/world/zones.json")
  private Resource zonesResource;

  @PostConstruct
  public void initializeZones() {
    log.info("Initializing default zones");

    ZonesConfig config = jsonLoader.load(zonesResource, ZonesConfig.class);
    zoneManager.clearZones();

    config.zones().forEach(zoneManager::addZone);

    log.info("Successfully initialized {} zones", config.zones().size());
  }
}

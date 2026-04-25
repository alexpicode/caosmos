package com.caosmos.world.application;

import com.caosmos.common.application.config.CaosmosResourceProperties;
import com.caosmos.common.domain.contracts.JsonLoader;
import com.caosmos.world.application.config.WorldObjectsConfig;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.service.SpatialHash;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldObjectInitializer {

  private final SpatialHash spatialHash;
  private final JsonLoader jsonLoader;
  private final CaosmosResourceProperties resourceProperties;

  @PostConstruct
  public void initializeDefaultObjects() {
    log.info("Initializing default world objects");

    WorldObjectsConfig config = jsonLoader.load(resourceProperties.world().objects(), WorldObjectsConfig.class);
    List<WorldObject> defaultObjects = config.worldObjects();

    defaultObjects.forEach(spatialHash::register);

    log.info("Successfully initialized {} world objects in spatial hash", defaultObjects.size());
  }

  public void addCustomObject(WorldObject worldObject) {
    spatialHash.register(worldObject);
  }
}

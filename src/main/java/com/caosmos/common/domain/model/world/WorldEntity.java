package com.caosmos.common.domain.model.world;

import java.util.Collections;
import java.util.Map;

public interface WorldEntity {

  String getId();

  String getType();

  Vector3 getPosition();

  String getName();

  String getZoneId();

  String getCategory();

  default Map<String, Object> getProperties() {
    return Collections.emptyMap();
  }
}

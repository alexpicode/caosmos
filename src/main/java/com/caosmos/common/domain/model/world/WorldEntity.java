package com.caosmos.common.domain.model.world;

import java.util.Collections;
import java.util.Map;

public interface WorldEntity {

  String getId();

  String getType();

  Vector3 getPosition();

  String getDisplayName();

  String getZoneId();

  default Map<String, Object> getProperties() {
    return Collections.emptyMap();
  }
}

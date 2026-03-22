package com.caosmos.common.domain.model.world;

public interface WorldEntity {

  String getId();

  String getType();

  Vector3 getPosition();

  String getDisplayName();

  default java.util.Map<String, Object> getProperties() {
    return java.util.Collections.emptyMap();
  }
}

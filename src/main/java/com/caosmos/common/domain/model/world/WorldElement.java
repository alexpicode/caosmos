package com.caosmos.common.domain.model.world;

import java.util.Collections;
import java.util.Set;

public interface WorldElement {

  String getId();

  String getType();

  Vector3 getPosition();

  String getName();

  String getZoneId();

  String getCategory();

  default Set<String> getTags() {
    return Collections.emptySet();
  }
}

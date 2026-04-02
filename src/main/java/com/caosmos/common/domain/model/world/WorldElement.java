package com.caosmos.common.domain.model.world;

public interface WorldElement {

  String getId();

  String getType();

  Vector3 getPosition();

  String getName();

  String getZoneId();

  String getCategory();
}

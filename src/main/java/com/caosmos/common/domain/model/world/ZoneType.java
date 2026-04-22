package com.caosmos.common.domain.model.world;

public enum ZoneType {
  EXTERIOR,
  INTERIOR,
  UNKNOWN;

  public static ZoneType from(String s) {
    if (s == null) {
      return UNKNOWN;
    }
    try {
      return ZoneType.valueOf(s.toUpperCase());
    } catch (IllegalArgumentException e) {
      return UNKNOWN;
    }
  }
}

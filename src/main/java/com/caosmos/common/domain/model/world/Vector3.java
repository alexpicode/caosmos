package com.caosmos.common.domain.model.world;

public record Vector3(
    double x,
    double y,
    double z
) {

  public double distanceTo(Vector3 other) {
    double dx = this.x - other.x;
    double dy = this.y - other.y;
    double dz = this.z - other.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public double distanceTo2D(Vector3 other) {
    double dx = this.x - other.x;
    double dz = this.z - other.z;
    return Math.sqrt(dx * dx + dz * dz);
  }

  public double getAngleTo(Vector3 other) {
    double dx = other.x - this.x;
    double dz = other.z - this.z;
    return Math.atan2(dz, dx);
  }
}

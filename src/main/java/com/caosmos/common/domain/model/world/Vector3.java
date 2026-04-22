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

  public double magnitude() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public Vector3 normalize() {
    double mag = magnitude();
    if (mag == 0) {
      return new Vector3(0, 0, 0);
    }
    return new Vector3(x / mag, y / mag, z / mag);
  }

  /**
   * Linear interpolation: this + t * (other - this)
   */
  public Vector3 lerp(Vector3 other, double t) {
    return new Vector3(
        this.x + (other.x - this.x) * t,
        this.y + (other.y - this.y) * t,
        this.z + (other.z - this.z) * t
    );
  }

  /**
   * Vector addition
   */
  public Vector3 add(Vector3 other) {
    return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
  }

  /**
   * Vector subtraction
   */
  public Vector3 subtract(Vector3 other) {
    return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
  }

  /**
   * Scalar multiplication
   */
  public Vector3 scale(double factor) {
    return new Vector3(this.x * factor, this.y * factor, this.z * factor);
  }
}

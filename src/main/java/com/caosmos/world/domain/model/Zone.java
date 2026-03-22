package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Zone {

  private String id;
  private String name;
  private String type; // e.g. INTERIOR, EXTERIOR
  private Vector3 center;
  private double width;
  private double length;

  public boolean contains(Vector3 position) {
    double halfWidth = width / 2.0;
    double halfLength = length / 2.0;
    return position.x() >= center.x() - halfWidth && position.x() <= center.x() + halfWidth &&
        position.z() >= center.z() - halfLength && position.z() <= center.z() + halfLength;
  }
}

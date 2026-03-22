package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorldObject implements WorldEntity {

  private String id;
  private String name;
  private Vector3 position;
  private List<String> tags;
  private String parentZoneId;

  @Override
  public String getType() {
    return "OBJECT";
  }

  @Override
  public String getDisplayName() {
    return name;
  }
}

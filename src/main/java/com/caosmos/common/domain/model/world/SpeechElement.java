package com.caosmos.common.domain.model.world;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SpeechElement implements WorldElement {

  private final String id;
  private final String sourceId;
  private final String sourceName;
  private final String targetId;
  private final String message;
  private final SpeechTone tone;
  private final Vector3 position;
  private final String zoneId;
  private final long startTick;

  @Override
  public EntityType getType() {
    return EntityType.SPEECH;
  }

  @Override
  public String getName() {
    return "Speech from " + sourceName;
  }

  @Override
  public String getCategory() {
    return "SPEECH";
  }

  @Override
  public NearbyElement toNearbyElement(double distance, String direction) {
    return new NearbyElement(
        id,
        getName(),
        getCategory(),
        EntityType.SPEECH,
        null,
        Math.round(distance * 100.0) / 100.0,
        direction,
        Set.of(tone.getValue()),
        getZoneId(),
        sourceId,
        targetId,
        message
    );
  }

  @Override
  public boolean isLimitedToZone() {
    return true;
  }

}

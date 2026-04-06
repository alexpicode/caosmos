package com.caosmos.common.domain.model.world;

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
  public String getType() {
    return "SPEECH";
  }

  @Override
  public String getName() {
    return sourceName + " says: " + message;
  }

  @Override
  public String getCategory() {
    return "SOCIAL";
  }
}

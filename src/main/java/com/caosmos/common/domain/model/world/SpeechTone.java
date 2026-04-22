package com.caosmos.common.domain.model.world;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpeechTone {
  NEUTRAL("neutral", false),
  WHISPER("whisper", false),
  CHEERFUL("cheerful", false),
  AGGRESSIVE("aggressive", true),
  URGENT("urgent", true),
  DIRECT_QUESTION("direct_question", true);

  private final String value;
  private final boolean interruptible;

  public static SpeechTone fromString(String text) {
    for (SpeechTone b : SpeechTone.values()) {
      if (b.value.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return NEUTRAL;
  }
}

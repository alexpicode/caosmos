package com.caosmos.citizens.domain.model.perception;

import com.caosmos.common.domain.model.world.SpeechTone;

public record SpeechMessage(
    String id,
    String sourceId,
    String sourceName,
    String targetId,
    String message,
    SpeechTone tone
) {

}

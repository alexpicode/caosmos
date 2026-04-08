package com.caosmos.citizens.domain.model.social;

public record DialogueLine(
    String speakerId,
    String speakerName,
    String message,
    long tick
) {

}

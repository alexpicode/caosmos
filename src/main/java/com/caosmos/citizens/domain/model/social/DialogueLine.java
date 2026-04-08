package com.caosmos.citizens.domain.model.social;

public record DialogueLine(
    String speakerId,
    String speakerName,
    String message,
    String tone,
    String targetId,
    // null = speaks to the group, "id" = addressed to someone
    long tick
) {

}

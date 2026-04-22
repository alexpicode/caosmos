package com.caosmos.citizens.application.dto;

public record SpeechMessageDto(
    String sourceName,
    String targetName,
    String message,
    String tone
) {

}

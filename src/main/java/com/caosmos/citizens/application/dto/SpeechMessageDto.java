package com.caosmos.citizens.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a speech message perceived or emitted")
public record SpeechMessageDto(
    @Schema(description = "Name of the entity that spoke") String sourceName,
    @Schema(description = "Name of the entity the speech is directed to, if any") String targetName,
    @Schema(description = "The actual spoken message") String message,
    @Schema(description = "The tone of the speech") String tone
) {

}

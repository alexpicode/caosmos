package com.caosmos.citizens.application.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caosmos.citizen")
public record ConversationConfigProperties(
    int maxConversationParticipants
) {

}

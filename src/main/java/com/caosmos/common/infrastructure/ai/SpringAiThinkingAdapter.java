package com.caosmos.common.infrastructure.ai;

import com.caosmos.common.application.telemetry.CognitionEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.ThinkingProvider;
import com.caosmos.common.domain.model.agents.AgentAction;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SpringAiThinkingAdapter implements ThinkingProvider {

  private final ChatClient sharedChatClient;
  private final BeanOutputConverter<AgentAction> outputConverter = new BeanOutputConverter<>(AgentAction.class);
  private final EntityTelemetryService telemetryService;

  public SpringAiThinkingAdapter(
      @Qualifier("thinkingChatClient") ChatClient thinkingChatClient,
      EntityTelemetryService telemetryService) {
    this.sharedChatClient = thinkingChatClient;
    this.telemetryService = telemetryService;
  }

  @Override
  public AgentAction think(UUID entityId, String entityName, long tick, String systemPrompt, String userMessage) {
    AgentAction action = sharedChatClient.prompt()
        .system(systemPrompt)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, entityId.toString()))
        .user(u -> u.text(userMessage + "\n" + outputConverter.getFormat()))
        .call()
        .entity(outputConverter);

    // Determine action target safely for telemetry
    String actionTarget = "none";
    if (action.params() != null) {
      Object targetId = action.params().get("targetId");
      Object item = action.params().get("item");

      if (targetId != null) {
        actionTarget = targetId.toString();
      } else if (item != null) {
        actionTarget = item.toString();
      } else {
        actionTarget = action.params().toString();
      }
    }

    telemetryService.registerThought(new CognitionEntry(
        entityId,
        tick,
        action.reasoning(),
        actionTarget
    ));

    return action;
  }
}

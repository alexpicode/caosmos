package com.caosmos.common.infrastructure.ai;

import com.caosmos.common.application.telemetry.CognitionEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.ThinkingProvider;
import com.caosmos.common.domain.model.agents.AgentAction;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Component
public class SpringAiThinkingAdapter implements ThinkingProvider {

  private final ChatClient sharedChatClient;
  private final BeanOutputConverter<AgentAction> outputConverter = new BeanOutputConverter<>(AgentAction.class);
  private final EntityTelemetryService telemetryService;

  public SpringAiThinkingAdapter(ChatClient.Builder chatClientBuilder, EntityTelemetryService telemetryService) {
    this.telemetryService = telemetryService;
    ChatMemory chatMemory = MessageWindowChatMemory.builder()
                                                   .maxMessages(10)
                                                   .build();

    this.sharedChatClient = chatClientBuilder
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
  }

  @Override
  public AgentAction think(UUID entityId, String entityName, long tick, String systemPrompt, String userMessage) {
    AgentAction action = sharedChatClient.prompt()
                                         .system(systemPrompt)
                                         .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, entityId.toString()))
                                         .user(u -> u.text(userMessage + "\n" + outputConverter.getFormat()))
                                         .call()
                                         .entity(outputConverter);

    //TODO: Refactor this to use a more robust way to get the action target
    String actionTarget = action.params().toString();
    if (action.params().containsKey("targetId")) {
      actionTarget = action.params().get("targetId").toString();
    } else if (action.params().containsKey("item")) {
      actionTarget = action.params().get("item").toString();
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
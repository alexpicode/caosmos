package com.caosmos.directors.infrastructure;

import com.caosmos.directors.domain.contracts.ObservationProvider;
import com.caosmos.directors.domain.model.ObservationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringAiObserverAdapter implements ObservationProvider {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final String systemPrompt;

  public SpringAiObserverAdapter(
      @Qualifier("directorChatClient") ChatClient directorChatClient,
      ObjectMapper objectMapper,
      @Value("classpath:/prompts/director-observer-system.md") Resource systemPromptResource
  ) throws IOException {
    this.chatClient = directorChatClient;
    this.objectMapper = objectMapper;
    this.systemPrompt = new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  @Override
  public String observe(ObservationRequest request) {
    try {
      String requestJson = objectMapper.writeValueAsString(request);

      String response = chatClient.prompt()
          .system(systemPrompt)
          .user(requestJson)
          .call()
          .content();

      JsonNode node = objectMapper.readTree(response);
      return node.get("description").asText();

    } catch (JsonProcessingException e) {
      log.error("Failed to serialize ObservationRequest", e);
      throw new RuntimeException("Failed to request observation", e);
    } catch (Exception e) {
      log.error("Failed to parse observation response", e);
      return "An unremarkable object."; // Fallback
    }
  }
}

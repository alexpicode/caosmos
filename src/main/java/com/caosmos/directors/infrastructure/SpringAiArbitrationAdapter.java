package com.caosmos.directors.infrastructure;

import com.caosmos.common.domain.model.actions.ResolutionResult;
import com.caosmos.directors.domain.contracts.ArbitrationProvider;
import com.caosmos.directors.domain.model.ArbitrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringAiArbitrationAdapter implements ArbitrationProvider {

  private final ChatClient chatClient;
  private final BeanOutputConverter<ResolutionResult> converter = new BeanOutputConverter<>(ResolutionResult.class);
  private final ObjectMapper objectMapper;
  private final String systemPrompt;

  public SpringAiArbitrationAdapter(
      ChatClient.Builder chatClientBuilder,
      ObjectMapper objectMapper,
      @Value("classpath:/prompts/director-arbitration-system.md") Resource systemPromptResource
  ) throws IOException {

    this.chatClient = chatClientBuilder.build();
    this.objectMapper = objectMapper;
    this.systemPrompt = new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  @Override
  public ResolutionResult arbitrate(ArbitrationRequest request) {
    try {
      // Serialize tags and details so the AI prompt can read them organically
      String requestJson = objectMapper.writeValueAsString(request);

      log.debug("Requesting Arbitration for: {}", requestJson);

      // Invoke the Spring AI conversational client setting the specific system behavior
      // of a physics judge, and appending the strict output format required by the BeanOutputConverter.
      return chatClient.prompt()
          .system(systemPrompt)
          .user(u -> u.text(requestJson + "\n" + converter.getFormat()))
          .call()
          .entity(converter);

    } catch (JsonProcessingException e) {
      log.error("Failed to serialize ArbitrationRequest", e);
      throw new RuntimeException("Failed to request arbitration", e);
    }
  }
}

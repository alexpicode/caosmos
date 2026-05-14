package com.caosmos.common.infrastructure.ai;

import com.google.genai.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfiguration {

  private static final Logger log = LoggerFactory.getLogger(AiModelConfiguration.class);

  @Bean
  @Qualifier("thinkingChatClient")
  public ChatClient thinkingChatClient(AiModelProperties aiModelProperties) {
    AiModelProperties.ChannelConfig cfg = aiModelProperties.getThinking();
    var chatModel = resolveChatModel(cfg, "thinking");
    log.info("Thinking provider initialized: {} (model={})", cfg.getProvider(), cfg.getModel());

    ChatMemory chatMemory = MessageWindowChatMemory.builder()
        .maxMessages(10)
        .build();

    return ChatClient.builder(chatModel)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
  }

  @Bean
  @Qualifier("directorChatClient")
  public ChatClient directorChatClient(AiModelProperties aiModelProperties) {
    AiModelProperties.ChannelConfig cfg = aiModelProperties.getDirector();
    var chatModel = resolveChatModel(cfg, "director");
    log.info("Director provider initialized: {} (model={})", cfg.getProvider(), cfg.getModel());
    return ChatClient.builder(chatModel).build();
  }

  private ChatModel resolveChatModel(
      AiModelProperties.ChannelConfig cfg, String channelName) {

    String provider = cfg.getProvider() != null ? cfg.getProvider().toLowerCase() : "gemini";
    String apiKey = cfg.getApiKey();
    String baseUrl = cfg.getBaseUrl();
    String model = cfg.getModel();
    double temperature = cfg.getTemperature();

    switch (provider) {
      case "ollama" -> {
        String effectiveBaseUrl = (baseUrl != null && !baseUrl.isEmpty()) ? baseUrl : "http://localhost:11434";
        String effectiveModel = (model != null && !model.isEmpty()) ? model : getEnv("OLLAMA_MODEL", "qwen3.5:9b");
        cfg.setBaseUrl(effectiveBaseUrl);
        cfg.setModel(effectiveModel);
        log.info("[{}] Using Ollama: baseUrl={}, model={}", channelName, effectiveBaseUrl, effectiveModel);
        OllamaApi api = OllamaApi.builder()
            .baseUrl(effectiveBaseUrl)
            .build();
        var options = OllamaChatOptions.builder()
            .model(effectiveModel)
            .temperature(temperature)
            .build();
        return OllamaChatModel.builder()
            .ollamaApi(api)
            .defaultOptions(options)
            .build();
      }
      case "openai" -> {
        String effectiveKey = (apiKey != null && !apiKey.isEmpty()) ? apiKey : getEnv("OPENAI_API_KEY", "");
        String effectiveModel = (model != null && !model.isEmpty()) ? model : getEnv("OPENAI_MODEL", "gpt-4o-mini");
        cfg.setModel(effectiveModel);
        log.info("[{}] Using OpenAI: model={}", channelName, effectiveModel);

        OpenAiApi api = OpenAiApi.builder().apiKey(effectiveKey).build();
        if (baseUrl != null && !baseUrl.isEmpty()) {
          api = OpenAiApi.builder().apiKey(effectiveKey).baseUrl(baseUrl).build();
          log.info("[{}] OpenAI baseUrl override: {}", channelName, baseUrl);
        }
        var options = OpenAiChatOptions.builder()
            .model(effectiveModel)
            .temperature(temperature)
            .build();
        return OpenAiChatModel.builder()
            .openAiApi(api)
            .defaultOptions(options)
            .build();
      }
      case "gemini" -> {
        String effectiveKey = (apiKey != null && !apiKey.isEmpty()) ? apiKey : getEnv("GOOGLE_AI_API_KEY", "");
        String effectiveModel = (model != null && !model.isEmpty()) ? model : getEnv("GOOGLE_AI_MODEL", "gemini-3.1-flash-lite-preview");
        cfg.setModel(effectiveModel);
        log.info("[{}] Using Gemini: model={}", channelName, effectiveModel);

        Client genAiClient = Client.builder()
            .apiKey(effectiveKey)
            .build();

        var options = GoogleGenAiChatOptions.builder()
            .model(effectiveModel)
            .temperature(temperature)
            .build();

        return GoogleGenAiChatModel.builder()
            .genAiClient(genAiClient)
            .defaultOptions(options)
            .build();
      }
      default ->
          throw new IllegalArgumentException(
              "Unknown AI provider '" + provider + "' for channel '" + channelName
                  + "'. Supported: gemini, ollama, openai");
    }
  }

  private static String getEnv(String key, String defaultValue) {
    String value = System.getenv(key);
    return (value != null && !value.isEmpty()) ? value : defaultValue;
  }
}

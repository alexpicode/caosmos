package com.caosmos.common.infrastructure.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "caosmos.ai")
public class AiModelProperties {

  private ChannelConfig thinking = new ChannelConfig();
  private ChannelConfig director = new ChannelConfig();

  public ChannelConfig getThinking() {
    return thinking;
  }

  public void setThinking(ChannelConfig thinking) {
    this.thinking = thinking;
  }

  public ChannelConfig getDirector() {
    return director;
  }

  public void setDirector(ChannelConfig director) {
    this.director = director;
  }

  public static class ChannelConfig {
    private String provider = "gemini";   // ollama | openai | gemini
    private String apiKey = "";
    private String baseUrl = "";
    private String model = "";
    private double temperature = 0.4;

    public String getProvider() {
      return provider;
    }

    public void setProvider(String provider) {
      this.provider = provider;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    public double getTemperature() {
      return temperature;
    }

    public void setTemperature(double temperature) {
      this.temperature = temperature;
    }
  }
}

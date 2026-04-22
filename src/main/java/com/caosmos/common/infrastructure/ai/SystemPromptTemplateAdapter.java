package com.caosmos.common.infrastructure.ai;

import com.caosmos.common.application.ai.SystemPromptTemplatePort;
import java.util.Map;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SystemPromptTemplateAdapter implements SystemPromptTemplatePort {

  private final StTemplateRenderer stTemplateRenderer;

  public SystemPromptTemplateAdapter() {
    this.stTemplateRenderer = StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build();
  }

  @Override
  public String buildMessage(Resource template, Map<String, Object> variables) {
    return SystemPromptTemplate.builder().resource(template).renderer(stTemplateRenderer).build()
                               .createMessage(variables).getText();
  }
}

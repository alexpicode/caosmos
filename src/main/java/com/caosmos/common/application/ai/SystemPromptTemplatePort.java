package com.caosmos.common.application.ai;

import java.util.Map;
import org.springframework.core.io.Resource;

public interface SystemPromptTemplatePort {

  String buildMessage(Resource template, Map<String, Object> variables);
}

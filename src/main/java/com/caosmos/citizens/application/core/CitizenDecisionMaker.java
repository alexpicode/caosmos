package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.model.PulseConfiguration;
import com.caosmos.citizens.application.model.PulseContext;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.common.application.ai.SystemPromptTemplatePort;
import com.caosmos.common.domain.contracts.ActionPort;
import com.caosmos.common.domain.contracts.JsonSerializer;
import com.caosmos.common.domain.contracts.ThinkingProvider;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.agents.AgentAction;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Handles decision-making process for citizens using AI reasoning. Manages prompt construction, AI interaction, and
 * action dispatching.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenDecisionMaker {

  private final ThinkingProvider thinkingProvider;
  private final SystemPromptTemplatePort promptTemplate;
  private final JsonSerializer jsonSerializer;
  private final ActionPort actionPort;

  /**
   * Makes a decision for a citizen based on their current state and world perception.
   */
  public LastAction makeDecision(Citizen citizen, PulseContext context, PulseConfiguration config) {
    String citizenName = citizen.getCitizenProfile().identity().name();

    // 1. Prepare Prompts
    String systemMessage = buildSystemMessage(citizen, config.systemPromptResource());
    String userMessage = buildUserMessage(citizen, context, config.userPromptResource());

    // 2. Set thinking state
    citizen.transitionTo(CitizenState.THINKING, "Deciding next action");

    // 3. Request Thinking from AI
    log.info("[CITIZEN:{}] Requesting AI Decision...", citizenName);
    AgentAction response = thinkingProvider.think(
        citizen.getUuid(),
        citizenName,
        context.tick(),
        systemMessage,
        userMessage
    );
    log.info(
        "[CITIZEN:{}] AI DECISION: {} - params: {} - reasoning: {}",
        citizenName,
        response.type(),
        response.params(),
        response.reasoning()
    );

    // 4. Dispatch Resulting Action
    ActionRequest request = new ActionRequest(response.type(), response.reasoning(), response.params());
    ActionResult result = actionPort.dispatch(citizen.getUuid(), request);
    log.info(
        "[CITIZEN:{}] DISPATCH RESULT: {} - {}",
        citizenName,
        result.success() ? "SUCCESS" : "FAILED",
        result.message()
    );

    // 5. Construct Domain Result
    return buildLastAction(request, result, response);
  }

  private String buildSystemMessage(Citizen citizen, Resource systemPromptResource) {
    Map<String, Object> systemMap = new HashMap<>();
    var identity = citizen.getCitizenProfile().identity();

    systemMap.put("name", identity.name());
    systemMap.put("job", identity.job());
    systemMap.put("workplace", identity.workplaceTag());
    systemMap.put("traits", String.join(", ", identity.traits()));
    systemMap.put("skills", jsonSerializer.toJson(identity.skills()));
    systemMap.put("personality", citizen.getCitizenProfile().personality());

    return promptTemplate.buildMessage(systemPromptResource, systemMap);
  }


  private String buildUserMessage(Citizen citizen, PulseContext context, Resource userPromptResource) {
    // 0. Extract perception from context (enriched with Mental Map) or domain (fallback)
    CitizenPerception perception = (context.fullPerception() != null)
        ? context.fullPerception().citizen()
        : citizen.getPerception();

    // 1. Self State (JSON)
    Map<String, Object> selfMap = new HashMap<>();
    selfMap.put("status", perception.status());
    selfMap.put("equipment", perception.equipment());
    selfMap.put("inventory", perception.inventory());
    selfMap.put("position", perception.position());
    selfMap.put("mental_map", perception.mentalMap());
    String selfJson = jsonSerializer.toJson(selfMap);
    log.debug("Self JSON: {}", selfJson);

    // 2. Contextual Data (JSON)
    Map<String, Object> contextualMap = new HashMap<>();
    contextualMap.put("current_state", citizen.getState());
    contextualMap.put("active_task", perception.activeTask());
    contextualMap.put("last_action", perception.lastAction());
    contextualMap.put("recent_events", context.unprocessedEvents());
    String contextualJson = jsonSerializer.toJson(contextualMap);

    // 3. Assemble Template
    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put("self_json", selfJson);
    messageMap.put("contextual_json", contextualJson);

    // Use world perception if available in context
    if (context.fullPerception() != null) {
      messageMap.put("world_json", jsonSerializer.toJson(context.fullPerception().world()));
      messageMap.put("explore_tags_json", jsonSerializer.toJson(context.fullPerception().world().tagsForExplore()));
    } else {
      messageMap.put("world_json", "{}");
      messageMap.put("explore_tags_json", "[]");
    }

    return promptTemplate.buildMessage(userPromptResource, messageMap);
  }

  private LastAction buildLastAction(ActionRequest request, ActionResult result, AgentAction response) {
    String status = result.success() ? "SUCCESS" : "FAILED";
    return new LastAction(
        request.type(),
        status,
        response.reasoning(),
        result.message() != null ? result.message() : response.reasoning(),
        response.params()
    );
  }
}

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
  public LastAction makeDecision(Citizen citizen, PulseContext context, PulseConfiguration pulseConfiguration) {

    String citizenName = citizen.getCitizenProfile().identity().name();

    // Prepare System Message
    String systemMessage = buildSystemMessage(citizen, pulseConfiguration.systemPromptResource());

    // Prepare User Message (Full Perception + Informative Events)
    String userMessage = buildUserMessage(citizen, context, pulseConfiguration.userPromptResource());

    // Set citizen state to thinking for the actual reasoning process
    citizen.transitionTo(CitizenState.THINKING, "Deciding what to do");

    log.info("[CITIZEN:{}] Thinking...", citizenName);

    // Delegate reasoning to ThinkingProvider
    AgentAction response = thinkingProvider.think(
        citizen.getUuid(),
        citizenName,
        context.tick(),
        systemMessage,
        userMessage
    );
    log.info("[CITIZEN:{}] Action Decided: {}", citizenName, response);

    // Dispatch action
    ActionRequest request = new ActionRequest(response.type(), response.params());
    ActionResult result = actionPort.dispatch(citizen.getUuid(), request);
    log.info("[CITIZEN:{}] Action Result: {}", citizenName, result);

    LastAction newLastAction = buildLastAction(request, result, response);

    return newLastAction;
  }


  /**
   * Builds the system prompt for AI reasoning.
   */
  private String buildSystemMessage(Citizen citizen, Resource systemPromptResource) {
    Map<String, Object> systemMap = new HashMap<>();
    systemMap.put("name", citizen.getCitizenProfile().identity().name());
    systemMap.put("traits", String.join(", ", citizen.getCitizenProfile().identity().traits()));
    systemMap.put("skills", jsonSerializer.toJson(citizen.getCitizenProfile().identity().skills()));
    systemMap.put("personality", citizen.getCitizenProfile().personality());

    return promptTemplate.buildMessage(systemPromptResource, systemMap);
  }

  /**
   * Builds the user message for AI reasoning.
   */
  private String buildUserMessage(Citizen citizen, PulseContext context, Resource userPromptResource) {

    CitizenPerception citizenPerception = context.fullPerception().citizen();

    Map<String, Object> selfMap = new HashMap<>();
    selfMap.put("identity", citizenPerception.identity());
    selfMap.put("status", citizenPerception.status());
    selfMap.put("equipment", citizenPerception.equipment());
    selfMap.put("inventory", citizenPerception.inventory());
    selfMap.put("position", citizenPerception.position());

    String selfJson = jsonSerializer.toJson(selfMap);

    Map<String, Object> contextualMap = new HashMap<>();
    contextualMap.put("current_state", citizen.getState());
    contextualMap.put("active_task", citizenPerception.activeTask());

    contextualMap.put("last_action", citizenPerception.lastAction());

    if (citizenPerception.lastAction() != null) {
      String status = citizenPerception.lastAction().status();
      if ("SUCCESS".equals(status) || "FAILED".equals(status) ||
          "CRITICAL_INTERRUPT".equals(status) || "ROUTINE_INTERRUPT".equals(status)) {
        contextualMap.put("interrupt_reason", status);
      }
    }

    contextualMap.put("events", context.unprocessedEvents());

    String contextualJson = jsonSerializer.toJson(contextualMap);

    Map<String, Object> messageMap = new HashMap<>();
    messageMap.put("self_json", selfJson);
    messageMap.put("contextual_json", contextualJson);
    messageMap.put("world_json", jsonSerializer.toJson(context.fullPerception().world()));

    return promptTemplate.buildMessage(userPromptResource, messageMap);
  }

  private static LastAction buildLastAction(ActionRequest request, ActionResult result, AgentAction response) {
    String status = result.success() ? "SUCCESS" : "FAILED";

    return new LastAction(request.type(), status, response.reasoning(), result.message(), response.params());
  }
}

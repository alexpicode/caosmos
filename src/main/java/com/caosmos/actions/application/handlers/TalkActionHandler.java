package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.SpeechElement;
import com.caosmos.common.domain.model.world.SpeechTone;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TalkActionHandler implements ActionHandler {

  private final CitizenPort citizenService;
  private final WorldPort worldPort;
  private final SimulationClock clock;

  @Override
  public String getActionType() {
    return "TALK";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");
    String message = (String) request.parameters().get("message");
    String toneStr = (String) request.parameters().get("tone");

    String targetName = null;
    if (targetId != null && !targetId.isBlank()) {
      try {
        targetName = citizenService.getName(UUID.fromString(targetId));
      } catch (IllegalArgumentException e) {
        targetName = targetId;
      }
    }

    if (message == null || message.isBlank()) {
      return ActionResult.failure("Message is required for TALK", getActionType());
    }

    // Check audible range if target is specified
    if (targetId != null && !targetId.isBlank()) {
      if (!citizenService.isNear(citizenId, targetId, ActionThresholds.AUDIBLE_RANGE)) {
        String name = (targetName != null) ? targetName : targetId;
        return ActionResult.failure("You are too far from " + name + " to hear you.", getActionType());
      }
    }

    SpeechTone tone = SpeechTone.fromString(toneStr);
    Vector3 sourcePos = citizenService.getPosition(citizenId);
    String sourceName = citizenService.getName(citizenId);
    String zoneId = citizenService.getCurrentZoneId(citizenId);

    SpeechElement speech = SpeechElement.builder()
        .id(UUID.randomUUID().toString())
        .sourceId(citizenId.toString())
        .sourceName(sourceName)
        .targetId(targetId)
        .message(message)
        .tone(tone)
        .position(sourcePos)
        .zoneId(zoneId)
        .startTick(clock.getCurrentTick())
        .build();

    worldPort.spawnSpeech(speech);

    if (targetId != null && !targetId.isBlank()) {
      citizenService.initiateOrJoinConversation(
          citizenId.toString(),
          sourceName,
          targetId,
          targetName,
          clock.getCurrentTick()
      );
    }
    citizenService.registerDialogue(citizenId.toString(), sourceName, message, clock.getCurrentTick());

    // Socializing reduces stress
    citizenService.reduceStress(citizenId, ActionThresholds.TALK_STRESS_REDUCTION);
    citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_TALK);

    String feedback = (targetId != null && !targetId.isBlank()) ?
        "Talking to " + (targetName != null ? targetName : targetId) + ": " + message :
        "Shouting: " + message;

    return ActionResult.success(feedback, getActionType());
  }
}

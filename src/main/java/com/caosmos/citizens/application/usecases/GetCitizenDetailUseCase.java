package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.core.CitizenSettings;
import com.caosmos.citizens.application.dto.CitizenConfigDto;
import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.dto.SpeechMessageDto;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.citizens.application.social.ConversationManager;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import com.caosmos.citizens.domain.model.social.ConversationSession;
import com.caosmos.citizens.domain.model.social.DialogueLine;
import com.caosmos.common.domain.contracts.SimulationClock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCitizenDetailUseCase {

  private final CitizenRegistry citizenRegistry;
  private final CitizenSettings citizenSettings;
  private final SimulationClock simulationClock;
  private final ConversationManager conversationManager;

  public Optional<CitizenDetailDto> execute(UUID uuid) {
    return citizenRegistry.get(uuid)
        .map(c -> {
          double realWorldWalkingSpeed = citizenSettings.getWalkingSpeed() * simulationClock.getDeltaTime();
          CitizenConfigDto config = new CitizenConfigDto(realWorldWalkingSpeed);

          // Get perceived messages (the ones the agent heard)
          List<SpeechMessageDto> perceived = c.getPerception().recentMessages().stream()
              .map(this::mapToDto)
              .toList();

          // Get session messages (sent and received in private/session context)
          List<SpeechMessageDto> sessionHistory = conversationManager.getActiveSession(uuid.toString())
              .map(session -> session.getHistory().stream()
                  .map(line -> mapSessionLineToDto(line, session, uuid.toString()))
                  .toList())
              .orElse(List.of());

          // Merge and deduplicate by message content (simple approach) or just show all
          List<SpeechMessageDto> allMessages = new ArrayList<>();
          allMessages.addAll(sessionHistory);

          // Add perceived messages that are not already in session history
          perceived.forEach(p -> {
            boolean alreadyInSession = sessionHistory.stream()
                .anyMatch(s -> s.message().equals(p.message()) && s.sourceName().equals(p.sourceName()));
            if (!alreadyInSession) {
              allMessages.add(p);
            }
          });

          return new CitizenDetailDto(
              config,
              c.getPerception(),
              c.getLastAction(),
              c.getCurrentState().getCurrentZone(),
              c.getVisitedZoneIds(),
              allMessages
          );
        });
  }

  private SpeechMessageDto mapToDto(SpeechMessage message) {
    String targetName = "Everyone";
    if (message.targetId() != null && !message.targetId().isBlank()) {
      try {
        UUID targetUuid = UUID.fromString(message.targetId());
        targetName = citizenRegistry.get(targetUuid)
            .map(c -> c.getCitizenProfile().identity().name())
            .orElse("Unknown (" + message.targetId() + ")");
      } catch (IllegalArgumentException e) {
        targetName = "Invalid Target (" + message.targetId() + ")";
      }
    }

    return new SpeechMessageDto(
        message.sourceName(),
        targetName,
        message.message(),
        message.tone() != null ? message.tone().getValue() : "neutral"
    );
  }

  private SpeechMessageDto mapSessionLineToDto(DialogueLine line, ConversationSession session, String myId) {
    String targetName;
    if (line.targetId() != null) {
      // Message addressed to someone specific
      targetName = session.getParticipants().getOrDefault(line.targetId(), "Unknown");
    } else {
      // Message to the group
      List<String> others = session.getOthers(line.speakerId()).values().stream().toList();
      targetName = others.size() == 1 ? others.get(0) : "Group";
    }

    return new SpeechMessageDto(
        line.speakerName(),
        targetName,
        line.message(),
        line.tone()
    );
  }
}

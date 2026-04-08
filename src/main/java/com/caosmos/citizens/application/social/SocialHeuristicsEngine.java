package com.caosmos.citizens.application.social;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialHeuristicsEngine {

  private final List<SpeechHeuristic> heuristics = new ArrayList<>();
  private final ConversationManager conversationManager;

  @PostConstruct
  public void init() {
    // 1. Session Turn Heuristic
    heuristics.add((citizen, messages) -> {
      String myId = citizen.getUuid().toString();
      var sessionOpt = conversationManager.getActiveSession(myId);
      if (sessionOpt.isPresent()) {
        var session = sessionOpt.get();
        // If it's my turn
        if (!myId.equals(session.getLastSpeakerId())) {
          // Is there a message from my partner?
          return messages.stream()
              .filter(m -> m.sourceId().equals(session.getPartnerId()) || m.sourceId().equals(session.getInitiatorId()))
              .findFirst();
        }
      }
      return Optional.empty();
    });

    // 2. Direct Message Heuristic
    heuristics.add((citizen, messages) -> {
      String myId = citizen.getUuid().toString();
      boolean allowsRoutine = citizen.getActiveTask() != null && citizen.getActiveTask().allowsRoutineInterruptions();

      return messages.stream()
          .filter(m -> myId.equals(m.targetId()))
          .filter(m -> m.tone().isInterruptible() || allowsRoutine)
          .findFirst();
    });

    // 3. Public Message Heuristic (only interrupts passive tasks)
    heuristics.add((citizen, messages) -> {
      boolean allowsRoutine = citizen.getActiveTask() != null && citizen.getActiveTask().allowsRoutineInterruptions();
      if (!allowsRoutine) {
        return Optional.empty();
      }
      return messages.stream()
          .filter(m -> m.targetId() == null || m.targetId().isBlank())
          // We can optionally filter by interruptible tones or not, but routine allows any tone
          .findFirst();
    });
  }

  public Optional<SpeechMessage> evaluate(Citizen citizen, List<SpeechMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return Optional.empty();
    }

    for (SpeechHeuristic heuristic : heuristics) {
      Optional<SpeechMessage> trigger = heuristic.evaluate(citizen, messages);
      if (trigger.isPresent()) {
        return trigger;
      }
    }

    return Optional.empty();
  }
}

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

  @PostConstruct
  public void init() {
    // 1. Tone and Target Heuristic
    heuristics.add((citizen, messages) -> {
      String myId = citizen.getUuid().toString();
      return messages.stream()
          .filter(m -> m.targetId() == null || m.targetId().isBlank() || m.targetId().equals(myId))
          .filter(m -> m.tone().isInterruptible())
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

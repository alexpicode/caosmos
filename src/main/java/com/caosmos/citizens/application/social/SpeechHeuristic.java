package com.caosmos.citizens.application.social;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface SpeechHeuristic {

  Optional<SpeechMessage> evaluate(Citizen citizen, List<SpeechMessage> messages);
}

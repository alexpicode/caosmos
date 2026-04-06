package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.core.CitizenSettings;
import com.caosmos.citizens.application.dto.CitizenConfigDto;
import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.dto.SpeechMessageDto;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import com.caosmos.common.domain.contracts.SimulationClock;
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

  public Optional<CitizenDetailDto> execute(UUID uuid) {
    return citizenRegistry.get(uuid)
        .map(c -> {
          double realWorldWalkingSpeed = citizenSettings.getWalkingSpeed() * simulationClock.getDeltaTime();
          CitizenConfigDto config = new CitizenConfigDto(realWorldWalkingSpeed);

          List<SpeechMessageDto> messageDtos = c.getPerception().recentMessages().stream()
              .map(this::mapToDto)
              .toList();

          return new CitizenDetailDto(
              config,
              c.getPerception(),
              c.getLastAction(),
              c.getCurrentState().getCurrentZone(),
              c.getVisitedZoneIds(),
              messageDtos
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
        message.tone().getValue()
    );
  }
}

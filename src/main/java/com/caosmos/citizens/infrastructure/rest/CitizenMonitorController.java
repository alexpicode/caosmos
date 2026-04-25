package com.caosmos.citizens.infrastructure.rest;

import com.caosmos.citizens.application.dto.CitizenCognitionDto;
import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.dto.CitizenInMapDto;
import com.caosmos.citizens.application.dto.CitizenSummaryDto;
import com.caosmos.citizens.application.usecases.GetCitizenCognitionUseCase;
import com.caosmos.citizens.application.usecases.GetCitizenDetailUseCase;
import com.caosmos.citizens.application.usecases.GetCitizensUseCase;
import com.caosmos.common.domain.contracts.SimulationClock;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CitizenMonitorController implements CitizenMonitorApi {

  private final GetCitizensUseCase getCitizensUseCase;
  private final GetCitizenDetailUseCase getCitizenDetailUseCase;
  private final GetCitizenCognitionUseCase getCitizenCognitionUseCase;
  private final SimulationClock simulationClock;

  @Override
  public ResponseEntity<List<CitizenSummaryDto>> getAllCitizens(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ,
      String name
  ) {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getCitizensUseCase.executeSummary(minX, minZ, maxX, maxZ, name));
  }

  @Override
  public ResponseEntity<List<CitizenInMapDto>> getCitizenInMap(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ
  ) {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getCitizensUseCase.executeInMap(minX, minZ, maxX, maxZ));
  }

  @Override
  public ResponseEntity<CitizenDetailDto> getCitizenDetail(UUID uuid) {
    return getCitizenDetailUseCase.execute(uuid)
        .map(dto -> ResponseEntity.ok()
            .header(
                "X-Sim-Tick",
                String.valueOf(simulationClock.getCurrentTick())
            )
            .body(dto))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<Collection<CitizenCognitionDto>> getCognition(
      UUID uuid,
      Long sinceTick
  ) {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getCitizenCognitionUseCase.execute(uuid, sinceTick));
  }
}

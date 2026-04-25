package com.caosmos.world.infrastructure.rest;

import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.world.application.dto.ChunkInfoDto;
import com.caosmos.world.application.dto.WorldEntitySummaryDto;
import com.caosmos.world.application.dto.WorldEnvironmentResponseDto;
import com.caosmos.world.application.dto.WorldObjectDetailDto;
import com.caosmos.world.application.dto.ZoneDto;
import com.caosmos.world.application.usecases.GetWorldChunksUseCase;
import com.caosmos.world.application.usecases.GetWorldEntitiesUseCase;
import com.caosmos.world.application.usecases.GetWorldEntityDetailUseCase;
import com.caosmos.world.application.usecases.GetWorldEnvironmentUseCase;
import com.caosmos.world.application.usecases.GetWorldZoneDetailUseCase;
import com.caosmos.world.application.usecases.GetWorldZonesUseCase;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WorldMonitorController implements WorldMonitorApi {

  private final GetWorldEntitiesUseCase getWorldEntitiesUseCase;
  private final GetWorldZonesUseCase getWorldZonesUseCase;
  private final GetWorldChunksUseCase getWorldChunksUseCase;
  private final GetWorldEnvironmentUseCase getWorldEnvironmentUseCase;
  private final GetWorldEntityDetailUseCase getWorldEntityDetailUseCase;
  private final GetWorldZoneDetailUseCase getWorldZoneDetailUseCase;
  private final SimulationClock simulationClock;

  @Override
  public ResponseEntity<List<ZoneDto>> getAllZones() {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getWorldZonesUseCase.execute());
  }

  @Override
  public ResponseEntity<ZoneDto> getZoneDetail(String id) {
    return getWorldZoneDetailUseCase.execute(id)
        .map(dto -> ResponseEntity.ok()
            .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
            .body(dto))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<WorldEntitySummaryDto>> getObjects(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ,
      String name,
      String category,
      String owned
  ) {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getWorldEntitiesUseCase.executeSummary(minX, minZ, maxX, maxZ, name, category, owned));
  }

  @Override
  public ResponseEntity<WorldObjectDetailDto> getObjectDetail(String id) {
    return getWorldEntityDetailUseCase.execute(id)
        .map(dto -> ResponseEntity.ok()
            .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
            .body(dto))
        .orElse(ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<ChunkInfoDto>> getChunks(
      double minX,
      double minZ,
      double maxX,
      double maxZ
  ) {
    List<ChunkInfoDto> dtos = getWorldChunksUseCase.execute(minX, minZ, maxX, maxZ)
        .stream()
        .map(c -> new ChunkInfoDto(
            c.gridX(),
            c.gridZ(),
            c.size(),
            c.entityCount(),
            c.movementCost()
        ))
        .collect(Collectors.toList());

    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(dtos);
  }

  @Override
  public ResponseEntity<WorldEnvironmentResponseDto> getEnvironment() {
    return ResponseEntity.ok()
        .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
        .body(getWorldEnvironmentUseCase.execute());
  }
}

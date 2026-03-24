package com.caosmos.world.infrastructure.rest;

import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.world.application.dto.WorldEntitySummaryDTO;
import com.caosmos.world.application.dto.WorldEnvironmentResponse;
import com.caosmos.world.application.usecases.GetWorldChunksUseCase;
import com.caosmos.world.application.usecases.GetWorldEntitiesUseCase;
import com.caosmos.world.application.usecases.GetWorldEnvironmentUseCase;
import com.caosmos.world.application.usecases.GetWorldZonesUseCase;
import com.caosmos.world.domain.model.ChunkInfo;
import com.caosmos.world.domain.model.Zone;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/world")
@RequiredArgsConstructor
public class WorldMonitorController {

  private final GetWorldEntitiesUseCase getWorldEntitiesUseCase;
  private final GetWorldZonesUseCase getWorldZonesUseCase;
  private final GetWorldChunksUseCase getWorldChunksUseCase;
  private final GetWorldEnvironmentUseCase getWorldEnvironmentUseCase;
  private final SimulationClock simulationClock;

  @GetMapping("/zones")
  @Operation(summary = "Get all world zones")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved zones",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<List<Zone>> getAllZones() {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getWorldZonesUseCase.execute());
  }

  @GetMapping("/entities")
  @Operation(summary = "Get world entities summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved entities summary",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<List<WorldEntitySummaryDTO>> getEntities(
      @RequestParam(required = false) Double minX,
      @RequestParam(required = false) Double minZ,
      @RequestParam(required = false) Double maxX,
      @RequestParam(required = false) Double maxZ,
      @RequestParam(required = false) String type
  ) {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getWorldEntitiesUseCase.executeSummary(minX, minZ, maxX, maxZ, type));
  }

  @GetMapping("/chunks")
  @Operation(summary = "Get world chunks info")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved chunks info",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<List<ChunkInfo>> getChunks(
      @RequestParam double minX,
      @RequestParam double minZ,
      @RequestParam double maxX,
      @RequestParam double maxZ
  ) {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getWorldChunksUseCase.execute(minX, minZ, maxX, maxZ));
  }

  @GetMapping("/environment")
  @Operation(summary = "Get world environment status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved environment status",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<WorldEnvironmentResponse> getEnvironment() {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getWorldEnvironmentUseCase.execute());
  }
}

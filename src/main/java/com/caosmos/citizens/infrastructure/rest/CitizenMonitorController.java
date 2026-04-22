package com.caosmos.citizens.infrastructure.rest;

import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.dto.CitizenInMapDto;
import com.caosmos.citizens.application.dto.CitizenSummaryDto;
import com.caosmos.citizens.application.usecases.GetCitizenCognitionUseCase;
import com.caosmos.citizens.application.usecases.GetCitizenDetailUseCase;
import com.caosmos.citizens.application.usecases.GetCitizensUseCase;
import com.caosmos.common.application.telemetry.CognitionEntry;
import com.caosmos.common.domain.contracts.SimulationClock;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citizens")
@RequiredArgsConstructor
public class CitizenMonitorController {

  private final GetCitizensUseCase getCitizensUseCase;
  private final GetCitizenDetailUseCase getCitizenDetailUseCase;
  private final GetCitizenCognitionUseCase getCitizenCognitionUseCase;
  private final SimulationClock simulationClock;

  @GetMapping
  @Operation(summary = "Get all citizens summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved citizens summary",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<List<CitizenSummaryDto>> getAllCitizens(
      @RequestParam(required = false) Double minX,
      @RequestParam(required = false) Double minZ,
      @RequestParam(required = false) Double maxX,
      @RequestParam(required = false) Double maxZ
  ) {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getCitizensUseCase.executeSummary(minX, minZ, maxX, maxZ));
  }

  @GetMapping("/map")
  @Operation(summary = "Get citizens for map display")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved citizens for map",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<List<CitizenInMapDto>> getCitizenInMap(
      @RequestParam(required = false) Double minX,
      @RequestParam(required = false) Double minZ,
      @RequestParam(required = false) Double maxX,
      @RequestParam(required = false) Double maxZ
  ) {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getCitizensUseCase.executeInMap(minX, minZ, maxX, maxZ));
  }

  @GetMapping("/{uuid}")
  @Operation(summary = "Get citizen details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved citizen details",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )}),
      @ApiResponse(responseCode = "404", description = "Citizen not found")
  })
  public ResponseEntity<CitizenDetailDto> getCitizenDetail(@PathVariable UUID uuid) {
    return getCitizenDetailUseCase.execute(uuid)
                                  .map(dto -> ResponseEntity.ok()
                                                            .header(
                                                                "X-Sim-Tick",
                                                                String.valueOf(simulationClock.getCurrentTick())
                                                            )
                                                            .body(dto))
                                  .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{uuid}/cognition")
  @Operation(summary = "Get citizen cognition history")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved cognition history",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  public ResponseEntity<Collection<CognitionEntry>> getCognition(
      @PathVariable UUID uuid,
      @RequestParam(required = false) Long sinceTick
  ) {
    return ResponseEntity.ok()
                         .header("X-Sim-Tick", String.valueOf(simulationClock.getCurrentTick()))
                         .body(getCitizenCognitionUseCase.execute(uuid, sinceTick));
  }
}

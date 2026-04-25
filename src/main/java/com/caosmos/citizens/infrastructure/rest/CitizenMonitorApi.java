package com.caosmos.citizens.infrastructure.rest;

import com.caosmos.citizens.application.dto.CitizenCognitionDto;
import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.dto.CitizenInMapDto;
import com.caosmos.citizens.application.dto.CitizenSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/citizens")
public interface CitizenMonitorApi {

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
  ResponseEntity<List<CitizenSummaryDto>> getAllCitizens(
      @Parameter(description = "Minimum X coordinate") @RequestParam(required = false) Double minX,
      @Parameter(description = "Minimum Z coordinate") @RequestParam(required = false) Double minZ,
      @Parameter(description = "Maximum X coordinate") @RequestParam(required = false) Double maxX,
      @Parameter(description = "Maximum Z coordinate") @RequestParam(required = false) Double maxZ,
      @Parameter(description = "Filter by citizen name (substring match)") @RequestParam(required = false) String name
  );

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
  ResponseEntity<List<CitizenInMapDto>> getCitizenInMap(
      @Parameter(description = "Minimum X coordinate") @RequestParam(required = false) Double minX,
      @Parameter(description = "Minimum Z coordinate") @RequestParam(required = false) Double minZ,
      @Parameter(description = "Maximum X coordinate") @RequestParam(required = false) Double maxX,
      @Parameter(description = "Maximum Z coordinate") @RequestParam(required = false) Double maxZ
  );

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
  ResponseEntity<CitizenDetailDto> getCitizenDetail(
      @Parameter(description = "Citizen unique identifier") @PathVariable("uuid") UUID uuid
  );

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
  ResponseEntity<Collection<CitizenCognitionDto>> getCognition(
      @Parameter(description = "Citizen unique identifier") @PathVariable("uuid") UUID uuid,
      @Parameter(description = "Fetch cognition history since this tick") @RequestParam(required = false) Long sinceTick
  );
}

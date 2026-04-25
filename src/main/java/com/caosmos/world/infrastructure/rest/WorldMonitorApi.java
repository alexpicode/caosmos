package com.caosmos.world.infrastructure.rest;

import com.caosmos.world.application.dto.ChunkInfoDto;
import com.caosmos.world.application.dto.WorldEntitySummaryDto;
import com.caosmos.world.application.dto.WorldEnvironmentResponseDto;
import com.caosmos.world.application.dto.WorldObjectDetailDto;
import com.caosmos.world.application.dto.ZoneDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/world")
public interface WorldMonitorApi {

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
  ResponseEntity<List<ZoneDto>> getAllZones();

  @GetMapping("/zones/{id}")
  @Operation(summary = "Get zone details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved zone details",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )}),
      @ApiResponse(responseCode = "404", description = "Zone not found")
  })
  ResponseEntity<ZoneDto> getZoneDetail(
      @Parameter(description = "Zone unique identifier") @PathVariable("id") String id
  );

  @GetMapping("/objects")
  @Operation(summary = "Get world objects summary")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved objects summary",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )})
  })
  ResponseEntity<List<WorldEntitySummaryDto>> getObjects(
      @Parameter(description = "Minimum X coordinate") @RequestParam(required = false) Double minX,
      @Parameter(description = "Minimum Z coordinate") @RequestParam(required = false) Double minZ,
      @Parameter(description = "Maximum X coordinate") @RequestParam(required = false) Double maxX,
      @Parameter(description = "Maximum Z coordinate") @RequestParam(required = false) Double maxZ,
      @Parameter(description = "Filter by object name (substring match)") @RequestParam(required = false) String name,
      @Parameter(description = "Filter by object category (exact match)") @RequestParam(required = false)
      String category,
      @Parameter(description = "Filter by object owner's name (substring match)") @RequestParam(required = false)
      String owned
  );

  @GetMapping("/objects/{id}")
  @Operation(summary = "Get object details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved object details",
          headers = {@Header(
              name = "X-Sim-Tick",
              description = "Current simulation tick",
              schema = @Schema(type = "integer")
          )}),
      @ApiResponse(responseCode = "404", description = "Object not found")
  })
  ResponseEntity<WorldObjectDetailDto> getObjectDetail(
      @Parameter(description = "Object unique identifier") @PathVariable("id") String id
  );

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
  ResponseEntity<List<ChunkInfoDto>> getChunks(
      @Parameter(description = "Minimum X coordinate", required = true) @RequestParam("minX") double minX,
      @Parameter(description = "Minimum Z coordinate", required = true) @RequestParam("minZ") double minZ,
      @Parameter(description = "Maximum X coordinate", required = true) @RequestParam("maxX") double maxX,
      @Parameter(description = "Maximum Z coordinate", required = true) @RequestParam("maxZ") double maxZ
  );

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
  ResponseEntity<WorldEnvironmentResponseDto> getEnvironment();
}

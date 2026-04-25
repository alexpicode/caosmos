package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Detailed representation of a world zone")
public record ZoneDto(
    @Schema(description = "Unique identifier of the zone") String id,
    @Schema(description = "Name of the zone") String name,
    @Schema(description = "Parent zone identifier if nested") String parentZoneId,
    @Schema(description = "Type of the zone (INTERIOR/EXTERIOR)") ZoneType type,
    @Schema(description = "Whether the zone requires specific entry points") boolean isEntryRestricted,
    @Schema(description = "Category of the zone") String category,
    @Schema(description = "Name of the owner, or null if unowned") String owned,
    @Schema(description = "Semantic tags associated with the zone") Set<String> tags,
    @Schema(description = "Center coordinates") Vector3 center,
    @Schema(description = "Width of the zone (X-axis)") double width,
    @Schema(description = "Length of the zone (Z-axis)") double length
) {

}

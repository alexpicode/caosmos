package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Summary representation of a world entity")
public record WorldEntitySummaryDto(
    @Schema(description = "Unique identifier of the entity") String id,
    @Schema(description = "Type of the entity (e.g. OBJECT, ZONE)") EntityType type,
    @Schema(description = "Display name of the entity") String displayName,
    @Schema(description = "Detailed description") String description,
    @Schema(description = "Category of the entity") String category,
    @Schema(description = "Name of the owner, or null if unowned") String owned,
    @Schema(description = "Semantic tags associated with the entity") Set<String> tags,
    @Schema(description = "X coordinate") double x,
    @Schema(description = "Y coordinate") double y,
    @Schema(description = "Z coordinate") double z
) {

}

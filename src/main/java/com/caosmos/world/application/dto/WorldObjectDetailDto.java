package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.Vector3;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Detailed representation of a world object")
public record WorldObjectDetailDto(
    @Schema(description = "Unique identifier of the object") String id,
    @Schema(description = "Type of the entity") EntityType type,
    @Schema(description = "Name of the object") String name,
    @Schema(description = "Detailed description") String description,
    @Schema(description = "Category of the object") String category,
    @Schema(description = "Name of the owner, or null if unowned") String owned,
    @Schema(description = "Semantic tags associated with the object") Set<String> tags,
    @Schema(description = "ID of the zone this object is located in") String parentZoneId,
    @Schema(description = "ID of the zone this gateway leads to (if applicable)") String targetZoneId,
    @Schema(description = "Position in the world") Vector3 position,
    @Schema(description = "Circular collision radius") Double radius,
    @Schema(description = "Rectangular collision width") Double width,
    @Schema(description = "Rectangular collision length") Double length,
    @Schema(description = "Quantity or amount") Double amount
) {

}

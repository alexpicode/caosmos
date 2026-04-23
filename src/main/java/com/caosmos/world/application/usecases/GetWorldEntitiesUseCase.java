package com.caosmos.world.application.usecases;

import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.world.application.dto.WorldEntitySummaryDto;
import com.caosmos.world.domain.service.SpatialHash;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEntitiesUseCase {

  private final SpatialHash spatialHash;
  private final CitizenRegistry citizenRegistry;

  public List<WorldEntitySummaryDto> executeSummary(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ,
      String name,
      String category,
      String owned
  ) {
    Collection<WorldElement> entities = getFilteredEntities(minX, minZ, maxX, maxZ);

    return entities.stream()
        .map(e -> {
          String ownerIdStr = e.getTags().stream()
              .filter(t -> t.startsWith(WorldConstants.PREFIX_OWNER))
              .findFirst()
              .map(t -> t.substring(WorldConstants.PREFIX_OWNER.length()))
              .orElse(null);

          String ownerName = null;
          if (ownerIdStr != null) {
            try {
              ownerName = citizenRegistry.get(UUID.fromString(ownerIdStr))
                  .map(c -> c.getCitizenProfile().identity().name())
                  .orElse(null); // Return null if owner not found
            } catch (IllegalArgumentException ex) {
              ownerName = null;
            }
          }

          Set<String> filteredTags = e.getTags().stream()
              .filter(t -> !t.startsWith(WorldConstants.PREFIX_OWNER))
              .collect(Collectors.toSet());

          return new WorldEntitySummaryDto(
              e.getId(),
              e.getType(),
              e.getName(),
              e.getDescription(),
              e.getCategory(),
              ownerName,
              filteredTags,
              e.getPosition().x(),
              e.getPosition().y(),
              e.getPosition().z()
          );
        })
        .filter(dto -> name == null || (dto.displayName() != null && dto.displayName()
            .toLowerCase()
            .contains(name.toLowerCase())))
        .filter(dto -> category == null || (dto.category() != null && dto.category().equalsIgnoreCase(category)))
        .filter(dto -> owned == null || (dto.owned() != null && dto.owned()
            .toLowerCase()
            .contains(owned.toLowerCase())))
        .toList();
  }

  private Collection<WorldElement> getFilteredEntities(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ
  ) {
    Collection<WorldElement> entities;
    if (minX != null && minZ != null && maxX != null && maxZ != null) {
      entities = spatialHash.getEntitiesInBoundingBox(minX, minZ, maxX, maxZ);
    } else {
      entities = spatialHash.getAllEntities();
    }

    // Only return objects of type "OBJECT"
    return entities.stream()
        .filter(e -> EntityType.OBJECT == e.getType())
        .toList();
  }
}

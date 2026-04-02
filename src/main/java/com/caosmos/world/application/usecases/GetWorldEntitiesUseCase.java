package com.caosmos.world.application.usecases;

import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.world.application.dto.WorldEntitySummaryDTO;
import com.caosmos.world.domain.service.SpatialHash;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEntitiesUseCase {

  private final SpatialHash spatialHash;

  public List<WorldEntitySummaryDTO> executeSummary(Double minX, Double minZ, Double maxX, Double maxZ, String type) {
    Collection<WorldElement> entities = getFilteredEntities(minX, minZ, maxX, maxZ, type);

    return entities.stream()
        .map(e -> new WorldEntitySummaryDTO(
            e.getId(),
            e.getType(),
            e.getName(),
            e.getPosition().x(),
            e.getPosition().y(),
            e.getPosition().z()
        ))
        .toList();
  }

  private Collection<WorldElement> getFilteredEntities(
      Double minX,
      Double minZ,
      Double maxX,
      Double maxZ,
      String type
  ) {
    Collection<WorldElement> entities;
    if (minX != null && minZ != null && maxX != null && maxZ != null) {
      entities = spatialHash.getEntitiesInBoundingBox(minX, minZ, maxX, maxZ);
    } else {
      entities = spatialHash.getAllEntities();
    }

    if (type != null && !type.isEmpty()) {
      return entities.stream().filter(e -> e.getType().equalsIgnoreCase(type)).toList();
    }
    return entities;
  }
}

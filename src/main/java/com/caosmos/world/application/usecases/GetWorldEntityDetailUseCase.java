package com.caosmos.world.application.usecases;

import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.world.application.dto.WorldObjectDetailDto;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.service.SpatialHash;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEntityDetailUseCase {

  private final SpatialHash spatialHash;
  private final CitizenRegistry citizenRegistry;

  public Optional<WorldObjectDetailDto> execute(String id) {
    return spatialHash.getAllEntities().stream()
        .filter(e -> e.getId().equals(id) && e instanceof WorldObject)
        .map(e -> (WorldObject) e)
        .findFirst()
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
                  .orElse(null);
            } catch (IllegalArgumentException ex) {
              ownerName = null;
            }
          }

          Set<String> filteredTags = e.getTags().stream()
              .filter(t -> !t.startsWith(WorldConstants.PREFIX_OWNER))
              .collect(Collectors.toSet());

          return new WorldObjectDetailDto(
              e.getId(),
              e.getType(),
              e.getName(),
              e.getDescription(),
              e.getCategory(),
              ownerName,
              filteredTags,
              e.getParentZoneId(),
              e.getTargetZoneId(),
              e.getPosition(),
              e.getRadius(),
              e.getWidth(),
              e.getLength(),
              e.getAmount()
          );
        });
  }
}

package com.caosmos.world.application.usecases;

import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.world.application.dto.ZoneDto;
import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldZoneDetailUseCase {

  private final ZoneManager zoneManager;
  private final CitizenRegistry citizenRegistry;

  public Optional<ZoneDto> execute(String id) {
    Optional<Zone> zoneOpt = zoneManager.getZone(id);
    if (zoneOpt.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(mapToDto(zoneOpt.get()));
  }

  private ZoneDto mapToDto(Zone zone) {
    String ownerIdStr = zone.getTags().stream()
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

    Set<String> filteredTags = zone.getTags().stream()
        .filter(t -> !t.startsWith(WorldConstants.PREFIX_OWNER))
        .collect(Collectors.toSet());

    return new ZoneDto(
        zone.getId(),
        zone.getName(),
        zone.getParentZoneId(),
        zone.getZoneType(),
        zone.isEntryRestricted(),
        zone.getCategory(),
        ownerName,
        filteredTags,
        zone.getCenter(),
        zone.getWidth(),
        zone.getLength()
    );
  }
}

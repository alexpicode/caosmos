package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.dto.CitizenInMapDto;
import com.caosmos.citizens.application.dto.CitizenSummaryDto;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.common.domain.contracts.WorldRegistry;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCitizensUseCase {

  private final CitizenRegistry citizenRegistry;
  private final WorldRegistry worldRegistry;

  public List<CitizenSummaryDto> executeSummary(Double minX, Double minZ, Double maxX, Double maxZ, String name) {
    Collection<Citizen> citizens = getFilteredCitizens(minX, minZ, maxX, maxZ);

    return citizens.stream()
        .filter(c -> name == null || c.getCitizenProfile().identity().name().toLowerCase().contains(name.toLowerCase()))
        .map(c -> new CitizenSummaryDto(
            c.getUuid(),
            c.getCitizenProfile().identity().name(),
            c.getPosition().x(),
            c.getPosition().y(),
            c.getPosition().z(),
            c.getState().name(),
            c.getActiveTask() != null ? c.getActiveTask().goal() : null,
            c.getPerception().status().vitality()
        ))
        .collect(Collectors.toList());
  }

  public List<CitizenInMapDto> executeInMap(Double minX, Double minZ, Double maxX, Double maxZ) {
    Collection<Citizen> citizens = getFilteredCitizens(minX, minZ, maxX, maxZ);

    return citizens.stream()
        .map(c -> new CitizenInMapDto(
            c.getUuid(),
            c.getPosition().x(),
            c.getPosition().z(),
            c.getState().name()
        ))
        .collect(Collectors.toList());
  }

  private Collection<Citizen> getFilteredCitizens(Double minX, Double minZ, Double maxX, Double maxZ) {
    if (minX != null && minZ != null && maxX != null && maxZ != null) {
      return worldRegistry.getEntitiesInBoundingBox(minX, minZ, maxX, maxZ)
          .stream()
          .filter(e -> e instanceof Citizen)
          .map(e -> (Citizen) e)
          .collect(Collectors.toList());
    }
    return citizenRegistry.getAll();
  }
}

package com.caosmos.world.application.usecases;

import com.caosmos.world.domain.model.ChunkInfo;
import com.caosmos.world.domain.service.SpatialHash;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldChunksUseCase {

  private final SpatialHash spatialHash;

  public List<ChunkInfo> execute(double minX, double minZ, double maxX, double maxZ) {
    return spatialHash.getChunksInBoundingBox(minX, minZ, maxX, maxZ);
  }
}

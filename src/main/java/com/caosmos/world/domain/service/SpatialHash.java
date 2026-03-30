package com.caosmos.world.domain.service;

import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.model.ChunkInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SpatialHash implements WorldRegistry {

  private static final double DEFAULT_CELL_SIZE = 10.0;

  private final double cellSize;
  private final Map<Long, Set<WorldEntity>> grid;
  private final Map<String, Long> objectToKeys;
  private final Map<String, WorldEntity> objectById;

  public SpatialHash() {
    this.cellSize = DEFAULT_CELL_SIZE;
    this.grid = new ConcurrentHashMap<>();
    this.objectToKeys = new ConcurrentHashMap<>();
    this.objectById = new ConcurrentHashMap<>();
  }

  private long hashCoordinates(int x, int z) {
    return ((long) x << 32) | (z & 0xFFFFFFFFL);
  }

  private void getCellCoordinates(Vector3 position, int[] coords) {
    coords[0] = (int) Math.floor(position.x() / cellSize);
    coords[1] = (int) Math.floor(position.z() / cellSize);
  }

  private Set<Long> getAffectedCells(Vector3 position, double radius) {
    Set<Long> cells = new HashSet<>();
    int minCellX = (int) Math.floor((position.x() - radius) / cellSize);
    int maxCellX = (int) Math.floor((position.x() + radius) / cellSize);
    int minCellZ = (int) Math.floor((position.z() - radius) / cellSize);
    int maxCellZ = (int) Math.floor((position.z() + radius) / cellSize);

    for (int x = minCellX; x <= maxCellX; x++) {
      for (int z = minCellZ; z <= maxCellZ; z++) {
        cells.add(hashCoordinates(x, z));
      }
    }
    return cells;
  }

  @Override
  public void register(WorldEntity obj) {
    remove(obj.getId());

    int[] coords = new int[2];
    getCellCoordinates(obj.getPosition(), coords);

    long key = hashCoordinates(coords[0], coords[1]);
    grid.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(obj);
    objectToKeys.put(obj.getId(), key);
    objectById.put(obj.getId(), obj);
  }

  @Override
  public void remove(String id) {
    WorldEntity obj = objectById.remove(id);
    if (obj != null) {
      Long key = objectToKeys.remove(id);
      if (key != null) {
        Set<WorldEntity> cell = grid.get(key);
        if (cell != null) {
          cell.remove(obj);
          if (cell.isEmpty()) {
            grid.remove(key, cell);
          }
        }
      }
    }
  }

  @Override
  public void updatePosition(WorldEntity entity, Vector3 newPosition) {
    register(entity);
  }

  public Set<WorldEntity> getNearbyEntities(Vector3 position, double radius) {
    Set<WorldEntity> nearbyEntities = new HashSet<>();
    Set<Long> affectedCells = getAffectedCells(position, radius);

    for (Long cellKey : affectedCells) {
      Set<WorldEntity> cell = grid.get(cellKey);
      if (cell != null) {
        for (WorldEntity obj : cell) {
          if (obj.getPosition().distanceTo2D(position) <= radius) {
            nearbyEntities.add(obj);
          }
        }
      }
    }

    return nearbyEntities;
  }

  public Set<WorldEntity> getEntitiesInBoundingBox(double minX, double minZ, double maxX, double maxZ) {
    Set<WorldEntity> entities = new HashSet<>();
    int minCellX = (int) Math.floor(minX / cellSize);
    int maxCellX = (int) Math.floor(maxX / cellSize);
    int minCellZ = (int) Math.floor(minZ / cellSize);
    int maxCellZ = (int) Math.floor(maxZ / cellSize);

    for (int x = minCellX; x <= maxCellX; x++) {
      for (int z = minCellZ; z <= maxCellZ; z++) {
        long key = hashCoordinates(x, z);
        Set<WorldEntity> cell = grid.get(key);
        if (cell != null) {
          for (WorldEntity obj : cell) {
            double px = obj.getPosition().x();
            double pz = obj.getPosition().z();
            if (px >= minX && px <= maxX && pz >= minZ && pz <= maxZ) {
              entities.add(obj);
            }
          }
        }
      }
    }
    return entities;
  }

  public List<ChunkInfo> getChunksInBoundingBox(double minX, double minZ, double maxX, double maxZ) {
    List<ChunkInfo> chunks = new ArrayList<>();
    int minCellX = (int) Math.floor(minX / cellSize);
    int maxCellX = (int) Math.floor(maxX / cellSize);
    int minCellZ = (int) Math.floor(minZ / cellSize);
    int maxCellZ = (int) Math.floor(maxZ / cellSize);

    for (int x = minCellX; x <= maxCellX; x++) {
      for (int z = minCellZ; z <= maxCellZ; z++) {
        long key = hashCoordinates(x, z);
        Set<WorldEntity> cell = grid.get(key);
        int entityCount = cell != null ? cell.size() : 0;
        // Basic movement cost initially 1.0
        chunks.add(new ChunkInfo(x, z, cellSize, entityCount, 1.0));
      }
    }
    return chunks;
  }

  public Optional<WorldEntity> getById(String id) {
    return Optional.ofNullable(objectById.get(id));
  }

  public Collection<WorldEntity> getAllEntities() {
    return objectById.values();
  }

  public void clear() {
    grid.clear();
    objectToKeys.clear();
    objectById.clear();
  }
}

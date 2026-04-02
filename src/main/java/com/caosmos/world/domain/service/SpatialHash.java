package com.caosmos.world.domain.service;

import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.world.domain.model.ChunkInfo;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * High-performance spatial indexing using a Grid-based Spatial Hash.
 * <p>
 * Performance characteristics: - O(1) registration and removal. - O(k) for spatial queries where k is the number of
 * cells covered by the area. - Uses 64-bit primitive long keys for cell indexing, avoiding object allocation overhead.
 * - Coordinates (x, z) are mapped to 32nd-bit integer space for the composite key.
 */
@Slf4j
@Service
public class SpatialHash implements WorldRegistry {

  private static final double CELL_SIZE = 10.0;

  /**
   * Grid mapping cell keys to sets of elements present in that cell.
   */
  private final Map<Long, Set<WorldElement>> grid = new ConcurrentHashMap<>();

  /**
   * Reverse mapping of element IDs to the cell keys they are currently registered in.
   */
  private final Map<String, Set<Long>> objectToKeys = new ConcurrentHashMap<>();

  /**
   * Direct registry of elements by their unique ID.
   */
  private final Map<String, WorldElement> objectById = new ConcurrentHashMap<>();

  @Override
  public void register(WorldElement obj) {
    String id = obj.getId();
    remove(id);

    Set<Long> keys = getKeysForEntity(obj);
    for (Long key : keys) {
      grid.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(obj);
    }
    objectToKeys.put(id, keys);
    objectById.put(id, obj);
  }

  @Override
  public void remove(String id) {
    WorldElement obj = objectById.remove(id);
    if (obj == null) {
      return;
    }

    Set<Long> keys = objectToKeys.remove(id);
    if (keys != null) {
      for (Long key : keys) {
        Set<WorldElement> cell = grid.get(key);
        if (cell != null) {
          cell.remove(obj);
          if (cell.isEmpty()) {
            grid.remove(key);
          }
        }
      }
    }
  }

  @Override
  public void updatePosition(WorldElement entity, Vector3 newPosition) {
    // Entities in this simulator typically update their position field directly,
    // so we re-register to ensure they are in the correct grid cell.
    register(entity);
  }

  public Set<WorldElement> getNearbyEntities(Vector3 position, double radius) {
    Set<WorldElement> result = new HashSet<>();
    forEachCellInRange(
        position.x() - radius, position.z() - radius,
        position.x() + radius, position.z() + radius,
        (key, cell) -> {
          if (cell != null) {
            for (WorldElement obj : cell) {
              if (position.distanceTo2D(obj.getPosition()) <= radius) {
                result.add(obj);
              }
            }
          }
        }
    );
    return result;
  }

  @Override
  public Set<WorldElement> getEntitiesInBoundingBox(double minX, double minZ, double maxX, double maxZ) {
    Set<WorldElement> result = new HashSet<>();
    forEachCellInRange(
        minX, minZ, maxX, maxZ, (key, cell) -> {
          if (cell != null) {
            for (WorldElement obj : cell) {
              Vector3 pos = obj.getPosition();
              if (pos.x() >= minX && pos.x() <= maxX && pos.z() >= minZ && pos.z() <= maxZ) {
                result.add(obj);
              }
            }
          }
        }
    );
    return result;
  }

  public List<ChunkInfo> getChunksInBoundingBox(double minX, double minZ, double maxX, double maxZ) {
    List<ChunkInfo> chunks = new ArrayList<>();
    forEachCellInRange(
        minX, minZ, maxX, maxZ, (key, cell) -> {
          int xi = getXi(key);
          int zi = getZi(key);
          int entityCount = cell != null ? cell.size() : 0;
          chunks.add(new ChunkInfo(xi, zi, CELL_SIZE, entityCount, 1.0));
        }
    );
    return chunks;
  }

  public Optional<WorldElement> getById(String id) {
    return Optional.ofNullable(objectById.get(id));
  }

  public Collection<WorldElement> getAllEntities() {
    return Collections.unmodifiableCollection(objectById.values());
  }

  public void clear() {
    grid.clear();
    objectToKeys.clear();
    objectById.clear();
  }

  /**
   * Combines two 32-bit grid indices into a single 64-bit long key.
   */
  private long toKey(long xi, long zi) {
    return (xi << 32) | (zi & 0xFFFFFFFFL);
  }

  /**
   * Generates a unique 64-bit key for a spatial cell. Uses bit-shifting to combine two 32-bit integers (truncated from
   * world coordinates).
   */
  private long getCellKey(double x, double z) {
    long xi = (long) Math.floor(x / CELL_SIZE);
    long zi = (long) Math.floor(z / CELL_SIZE);
    return toKey(xi, zi);
  }

  /**
   * Identifies all cell keys that an element should be registered in. If the element is a Zone, it returns all keys
   * covered by its area. Otherwise, it returns a single key for its point position.
   */
  private Set<Long> getKeysForEntity(WorldElement obj) {
    if (obj instanceof Zone zone) {
      double halfWidth = zone.getWidth() / 2.0;
      double halfLength = zone.getLength() / 2.0;
      Vector3 center = zone.getCenter();
      return getKeysForArea(
          center.x() - halfWidth,
          center.z() - halfLength,
          center.x() + halfWidth,
          center.z() + halfLength
      );
    }
    return Set.of(getCellKey(obj.getPosition().x(), obj.getPosition().z()));
  }

  /**
   * Returns all cell keys that intersect with the specified rectangular area.
   */
  private Set<Long> getKeysForArea(double minX, double minZ, double maxX, double maxZ) {
    Set<Long> keys = new HashSet<>();
    long minXi = (long) Math.floor(minX / CELL_SIZE);
    long minZi = (long) Math.floor(minZ / CELL_SIZE);
    long maxXi = (long) Math.floor(maxX / CELL_SIZE);
    long maxZi = (long) Math.floor(maxZ / CELL_SIZE);

    for (long xi = minXi; xi <= maxXi; xi++) {
      for (long zi = minZi; zi <= maxZi; zi++) {
        keys.add(toKey(xi, zi));
      }
    }
    return keys;
  }

  /**
   * Traverses all cells within a specified rectangular range and applies an action.
   */
  private void forEachCellInRange(
      double minX,
      double minZ,
      double maxX,
      double maxZ,
      BiConsumer<Long, Set<WorldElement>> action
  ) {
    long minXi = (long) Math.floor(minX / CELL_SIZE);
    long minZi = (long) Math.floor(minZ / CELL_SIZE);
    long maxXi = (long) Math.floor(maxX / CELL_SIZE);
    long maxZi = (long) Math.floor(maxZ / CELL_SIZE);

    for (long xi = minXi; xi <= maxXi; xi++) {
      for (long zi = minZi; zi <= maxZi; zi++) {
        long key = toKey(xi, zi);
        Set<WorldElement> cell = grid.get(key);
        action.accept(key, cell);
      }
    }
  }

  /**
   * Extracts the X grid index from a 64-bit key.
   */
  private int getXi(long key) {
    return (int) (key >> 32);
  }

  /**
   * Extracts the Z grid index from a 64-bit key.
   */
  private int getZi(long key) {
    return (int) (key & 0xFFFFFFFFL);
  }
}

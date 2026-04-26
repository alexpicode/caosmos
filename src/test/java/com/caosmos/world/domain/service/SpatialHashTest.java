package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.ChunkInfo;
import com.caosmos.world.domain.model.WorldObject;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SpatialHashTest {

  private SpatialHash spatialHash;

  @BeforeEach
  void setUp() {
    spatialHash = new SpatialHash();
  }

  @Test
  void testGetChunksShouldOnlyReturnNonEmptyChunks() {
    // Given 2 objects in different cells
    WorldObject obj1 = createObject(new Vector3(5, 0, 5)); // Cell (0,0)
    WorldObject obj2 = createObject(new Vector3(15, 0, 15)); // Cell (1,1)
    spatialHash.register(obj1);
    spatialHash.register(obj2);

    // When requesting a large area
    List<ChunkInfo> chunks = spatialHash.getChunksInBoundingBox(-100, -100, 100, 100);

    // Then (currently this fails and returns many empty chunks)
    // We want only 2 chunks
    assertEquals(2, chunks.size(), "Should only return non-empty chunks");
  }

  @Test
  @Timeout(value = 2)
    // Should complete in less than 2 seconds
  void testHugeBoundingBoxPerformance() {
    // Given 1 object
    spatialHash.register(createObject(new Vector3(0, 0, 0)));

    // When requesting an extremely large area (that would cause 2^64 iterations if not optimized)
    // Using a large but safe range for the first check
    long start = System.currentTimeMillis();
    spatialHash.getChunksInBoundingBox(-1000000, -1000000, 1000000, 1000000);
    long end = System.currentTimeMillis();

    assertTrue((end - start) < 500, "Querying a huge area should be fast (less than 500ms)");
  }

  private WorldObject createObject(Vector3 pos) {
    String id = UUID.randomUUID().toString();
    return new WorldObject(
        id, "Obj-" + id, "RESOURCE",
        pos,
        java.util.Set.of(), "Test Obj", null, null, null, null, null, null
    );
  }
}

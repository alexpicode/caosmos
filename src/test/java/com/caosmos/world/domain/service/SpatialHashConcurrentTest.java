package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.WorldObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SpatialHashConcurrentTest {

  @Test
  void testConcurrentRegistrationAndQueries() throws InterruptedException {
    SpatialHash spatialHash = new SpatialHash();
    int threadCount = 20;
    int operationsPerThread = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    assertDoesNotThrow(() -> {
      for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
          for (int j = 0; j < operationsPerThread; j++) {
            String id = UUID.randomUUID().toString();
            WorldObject obj = new WorldObject();
            obj.setId(id);
            obj.setPosition(new Vector3(Math.random() * 100, 0, Math.random() * 100));
            
            // Randomly perform different operations
            double choice = Math.random();
            if (choice < 0.4) {
              spatialHash.register(obj);
            } else if (choice < 0.7) {
              spatialHash.getNearbyEntities(new Vector3(50, 0, 50), 20);
            } else {
              spatialHash.remove(id);
            }
          }
        });
      }

      executor.shutdown();
      executor.awaitTermination(30, TimeUnit.SECONDS);
    });
  }
}

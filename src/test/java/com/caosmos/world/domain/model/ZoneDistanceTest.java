package com.caosmos.world.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ZoneDistanceTest {

  @Test
  void testDistanceToPerimeter() {
    // Zone centered at (0,0) with width 10 (x: -5 to 5) and length 20 (z: -10 to 10)
    Zone zone = new Zone(
        "test-zone", "Test Zone", null, ZoneType.EXTERIOR, "TEST",
        Set.of(), Set.of(), false, new Vector3(0, 0, 0), 10, 20
    );

    // 1. Inside: distance should be 0
    assertEquals(0.0, zone.distanceTo2D(new Vector3(0, 0, 0)), 0.001);
    assertEquals(0.0, zone.distanceTo2D(new Vector3(4, 9, 0)), 0.001);
    assertEquals(0.0, zone.distanceTo2D(new Vector3(-4, -9, 0)), 0.001);

    // 2. On the edge: distance should be 0
    assertEquals(0.0, zone.distanceTo2D(new Vector3(5, 0, 0)), 0.001);
    assertEquals(0.0, zone.distanceTo2D(new Vector3(0, 10, 0)), 0.001);

    // 3. Outside - axis aligned
    assertEquals(5.0, zone.distanceTo2D(new Vector3(10, 0, 0)), 0.001); // 10 - 5 = 5
    assertEquals(5.0, zone.distanceTo2D(new Vector3(-10, 0, 0)), 0.001); // |-10| - 5 = 5
    assertEquals(10.0, zone.distanceTo2D(new Vector3(0, 0, 20)), 0.001); // 20 - 10 = 10
    assertEquals(10.0, zone.distanceTo2D(new Vector3(0, 0, -20)), 0.001); // |-20| - 10 = 10

    // 4. Outside - corner
    // Point at (8, 0, 14).
    // dx = 8 - 5 = 3
    // dz = 14 - 10 = 4
    // dist = sqrt(3^2 + 4^2) = 5
    assertEquals(5.0, zone.distanceTo2D(new Vector3(8, 0, 14)), 0.001);
  }
}

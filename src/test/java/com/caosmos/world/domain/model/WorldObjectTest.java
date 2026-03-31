package com.caosmos.world.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WorldObjectTest {

  @Test
  void testCircleIntersection() {
    WorldObject obj = new WorldObject(
        "tree",
        "Tree",
        "NATURE",
        new Vector3(10, 0, 10),
        Set.of(),
        null,
        2.0,
        null,
        null
    );

    assertTrue(obj.intersects(new Vector3(11, 0, 11)), "Point (11,11) should be inside radius 2.0 at (10,10)");
    assertFalse(obj.intersects(new Vector3(13, 0, 10)), "Point (13,10) should be outside radius 2.0 at (10,10)");
  }

  @Test
  void testRectangularIntersection() {
    WorldObject obj = new WorldObject(
        "bench",
        "Bench",
        "FURNITURE",
        new Vector3(0, 0, 0),
        Set.of(),
        null,
        null,
        4.0,
        2.0
    );

    assertTrue(obj.intersects(new Vector3(1.5, 0, 0.5)), "Point (1.5, 0.5) should be inside 4x2 box at (0,0)");
    assertTrue(obj.intersects(new Vector3(-2, 0, -1)), "Edge point (-2, -1) should be inside 4x2 box at (0,0)");
    assertFalse(obj.intersects(new Vector3(2.1, 0, 0)), "Point (2.1, 0) should be outside 4x2 box at (0,0)");
    assertFalse(obj.intersects(new Vector3(0, 0, 1.1)), "Point (0, 1.1) should be outside 4x2 box at (0,0)");
  }

  @Test
  void testDefaultPointIntersection() {
    WorldObject obj = new WorldObject();
    obj.setPosition(new Vector3(0, 0, 0));

    assertTrue(obj.intersects(new Vector3(0.05, 0, 0.05)), "Point very close to center should intersect");
    assertFalse(obj.intersects(new Vector3(0.5, 0, 0.5)), "Point further away should not intersect by default");
  }
}

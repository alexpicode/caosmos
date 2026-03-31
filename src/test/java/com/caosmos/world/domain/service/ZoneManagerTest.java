package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.Zone;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZoneManagerTest {

  private ZoneManager zoneManager;

  @BeforeEach
  void setUp() {
    zoneManager = new ZoneManager();
  }

  @Test
  void testFindZoneAt_Simple() {
    Zone zone = new Zone(
        "z1",
        "Zone 1",
        null,
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of(),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );
    zoneManager.addZone(zone);

    Optional<Zone> found = zoneManager.findZoneAt(new Vector3(0, 0, 0), null);
    assertTrue(found.isPresent());
    assertEquals("z1", found.get().getId());
  }

  @Test
  void testFindZoneAt_HierarchyPriority() {
    Zone outer = new Zone(
        "outer",
        "Outer",
        null,
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of("outer_tag"),
        false,
        new Vector3(0, 0, 0),
        20,
        20
    );
    Zone inner = new Zone(
        "inner",
        "Inner",
        "outer",
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of("inner_tag"),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );

    zoneManager.addZone(outer);
    zoneManager.addZone(inner);

    // Position inside both zones should return the inner one (deeper)
    Optional<Zone> found = zoneManager.findZoneAt(new Vector3(0, 0, 0), null);
    assertTrue(found.isPresent());
    assertEquals("inner", found.get().getId());
  }

  @Test
  void testFindZoneAt_RestrictedEntry() {
    Zone outer = new Zone(
        "outer",
        "Outer",
        null,
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of(),
        false,
        new Vector3(0, 0, 0),
        20,
        20
    );
    Zone restricted = new Zone(
        "restricted",
        "Restricted",
        "outer",
        "INTERIOR",
        "TEST",
        Set.of(),
        Set.of(),
        true,
        new Vector3(0, 0, 0),
        10,
        10
    );

    zoneManager.addZone(outer);
    zoneManager.addZone(restricted);

    // Position inside restricted zone, but no currentZoneId provided -> returns outer
    Optional<Zone> foundNoAccess = zoneManager.findZoneAt(new Vector3(0, 0, 0), null);
    assertTrue(foundNoAccess.isPresent());
    assertEquals("outer", foundNoAccess.get().getId());

    // Position inside restricted zone, with currentZoneId matching -> returns restricted
    Optional<Zone> foundWithAccess = zoneManager.findZoneAt(new Vector3(0, 0, 0), "restricted");
    assertTrue(foundWithAccess.isPresent());
    assertEquals("restricted", foundWithAccess.get().getId());
  }

  @Test
  void testEffectiveTags() {
    Zone root = new Zone(
        "root",
        "Root",
        null,
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of("a"),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );
    Zone child = new Zone(
        "child",
        "Child",
        "root",
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of("b"),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );
    Zone grandchild = new Zone(
        "grandchild",
        "Grandchild",
        "child",
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of("c"),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );

    zoneManager.addZone(root);
    zoneManager.addZone(child);
    zoneManager.addZone(grandchild);

    Set<String> tags = grandchild.getEffectiveTags(zoneManager.getZoneMap());
    assertEquals(3, tags.size());
    assertTrue(tags.contains("a"));
    assertTrue(tags.contains("b"));
    assertTrue(tags.contains("c"));
  }

  @Test
  void testHierarchyDepth() {
    Zone root = new Zone(
        "root",
        "Root",
        null,
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of(),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );
    Zone child = new Zone(
        "child",
        "Child",
        "root",
        "EXTERIOR",
        "TEST",
        Set.of(),
        Set.of(),
        false,
        new Vector3(0, 0, 0),
        10,
        10
    );

    zoneManager.addZone(root);
    zoneManager.addZone(child);

    assertEquals(0, root.getHierarchyDepth(zoneManager.getZoneMap()));
    assertEquals(1, child.getHierarchyDepth(zoneManager.getZoneMap()));
  }
}

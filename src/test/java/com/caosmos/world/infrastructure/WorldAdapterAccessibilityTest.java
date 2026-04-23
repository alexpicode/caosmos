package com.caosmos.world.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.EnvironmentNormalizer;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.SpeechManager;
import com.caosmos.world.domain.service.VisualCoverageCalculator;
import com.caosmos.world.domain.service.ZoneCollisionService;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorldAdapterAccessibilityTest {

  @Mock
  private SpatialHash spatialHash;
  @Mock
  private ZoneManager zoneManager;
  @Mock
  private SpeechManager speechManager;
  @Mock
  private EnvironmentService environmentService;
  @Mock
  private EnvironmentNormalizer environmentNormalizer;
  @Mock
  private ZoneCollisionService zoneCollisionService;
  @Mock
  private VisualCoverageCalculator visualCoverageCalculator;

  private WorldAdapter worldAdapter;

  @BeforeEach
  void setUp() {
    worldAdapter = new WorldAdapter(
        spatialHash,
        zoneManager,
        speechManager,
        environmentService,
        environmentNormalizer,
        zoneCollisionService,
        visualCoverageCalculator
    );
  }

  @Test
  void testGatewayIsAccessibleFromBothSides() {
    // 1. Setup: Town Square (Exterior) and a House (Interior)
    Zone town = createZone("town", "Town Square", null, ZoneType.EXTERIOR);
    Zone house = createZone("house", "House", "town", ZoneType.INTERIOR);

    // 2. Setup: A door object registered in "town" but pointing to "house"
    WorldObject door = new WorldObject(
        "door_id",
        "Front Door",
        "GATEWAY",
        new Vector3(100, 0, 80),
        Set.of(),
        "The entrance to the house",
        "town", // parentZoneId
        "house", // targetZoneId
        1.0, null, null, null
    );

    when(spatialHash.getById("door_id")).thenReturn(Optional.of(door));
    lenient().when(zoneManager.getZone("town")).thenReturn(Optional.of(town));
    lenient().when(zoneManager.getZone("house")).thenReturn(Optional.of(house));

    // 3. Test: Citizen at (100,0,80) in "town" (Same zone as door)
    assertTrue(
        worldAdapter.isNearObject(new Vector3(100, 0, 80), "town", "door_id", 2.5),
        "Door should be accessible from the zone it belongs to (town)"
    );

    // 4. Test: Citizen at (100,0,80) in "house" (Target zone of door)
    // BEFORE FIX: This would return false because "house" is an INTERIOR and the door is in "town"
    assertTrue(
        worldAdapter.isNearObject(new Vector3(100, 0, 80), "house", "door_id", 2.5),
        "Door should be accessible from its target zone (house) even if it's an interior"
    );
  }

  @Test
  void testGenericObjectStillHiddenFromInterior() {
    // 1. Setup
    Zone town = createZone("town", "Town Square", null, ZoneType.EXTERIOR);
    Zone house = createZone("house", "House", "town", ZoneType.INTERIOR);

    // 2. Setup: A tree in "town"
    WorldObject tree = new WorldObject(
        "tree_id",
        "Tree",
        "NATURE",
        new Vector3(110, 0, 90),
        Set.of(),
        "A nice tree",
        "town",
        null, // NOT a gateway
        1.0, null, null, null
    );

    when(spatialHash.getById("tree_id")).thenReturn(Optional.of(tree));
    when(zoneManager.getZone("town")).thenReturn(Optional.of(town));
    when(zoneManager.getZone("house")).thenReturn(Optional.of(house));

    // 3. Test: Citizen in house trying to reach tree in town
    assertFalse(
        worldAdapter.isNearObject(new Vector3(110, 0, 90), "house", "tree_id", 2.5),
        "Generic objects in exterior should STILL be hidden from interior"
    );
  }

  private Zone createZone(String id, String name, String parentId, ZoneType type) {
    return new Zone(id, name, parentId, type, "TEST", Set.of(), Set.of(), false, new Vector3(0, 0, 0), 0, 0);
  }
}

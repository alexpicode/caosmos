package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NearbyEntityServiceTest {

  @Mock
  private SpatialHash spatialHash;
  @Mock
  private DirectionCalculator directionCalculator;
  @Mock
  private ZoneManager zoneManager;
  @Mock
  private NearbyZoneService nearbyZoneService;

  private NearbyEntityService nearbyEntityService;

  @BeforeEach
  void setUp() {
    nearbyEntityService = new NearbyEntityService(spatialHash, directionCalculator, zoneManager, nearbyZoneService);
    lenient().when(directionCalculator.getCardinalDirection(any(), any())).thenReturn("North");
  }

  @Test
  void testEntityVisibilityInSameZone() {
    Zone town = createZone("town", "Town Center", null, "EXTERIOR");
    WorldObject anvil = createObject("anvil", "Anvil", "town", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldEntity>of(anvil));

    List<NearbyEntity> entities = nearbyEntityService.getNearbyEntitiesOrdered(new Vector3(0, 0, 0), 10, "town", null);
    assertEquals(1, entities.size());
    assertEquals("anvil", entities.get(0).id());
  }

  @Test
  void testEntityVisibilityFromExteriorToChildInterior() {
    Zone town = createZone("town", "Town Center", null, "EXTERIOR");
    Zone blacksmith = createZone("blacksmith", "Blacksmith", "town", "INTERIOR");
    WorldObject anvil = createObject("anvil", "Anvil", "blacksmith", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "blacksmith", blacksmith));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldEntity>of(anvil));
    when(nearbyZoneService.isZoneVisible("blacksmith", "town")).thenReturn(true);

    List<NearbyEntity> entities = nearbyEntityService.getNearbyEntitiesOrdered(new Vector3(0, 0, 0), 10, "town", null);

    assertTrue(entities.isEmpty(), "Entities inside an INTERIOR should be hidden from the outside");
  }

  @Test
  void testEntityVisibilityFromInteriorToParentExterior() {
    Zone town = createZone("town", "Town Center", null, "EXTERIOR");
    Zone blacksmith = createZone("blacksmith", "Blacksmith", "town", "INTERIOR");
    WorldObject barrel = createObject("barrel", "Barrel", "town", 0, 0);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "blacksmith", blacksmith));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldEntity>of(barrel));
    when(nearbyZoneService.isZoneVisible("town", "blacksmith")).thenReturn(true);

    List<NearbyEntity> entities = nearbyEntityService.getNearbyEntitiesOrdered(
        new Vector3(5, 5, 0),
        10,
        "blacksmith",
        null
    );

    assertEquals(1, entities.size());
    assertEquals("barrel", entities.get(0).id());
  }

  private Zone createZone(String id, String name, String parentId, String type) {
    return new Zone(id, name, parentId, type, Set.of(), Set.of(), false, new Vector3(0, 0, 0), 0, 0);
  }

  private WorldObject createObject(String id, String name, String zoneId, double x, double z) {
    WorldObject obj = new WorldObject();
    obj.setId(id);
    obj.setName(name);
    obj.setParentZoneId(zoneId);
    obj.setPosition(new Vector3(x, 0, z));
    obj.setTags(Set.of());
    return obj;
  }
}

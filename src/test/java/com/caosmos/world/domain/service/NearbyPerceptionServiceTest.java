package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.domain.model.PeripheralPerception;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NearbyPerceptionServiceTest {

  @Mock
  private SpatialHash spatialHash;
  @Mock
  private DirectionCalculator directionCalculator;
  @Mock
  private ZoneManager zoneManager;

  private NearbyPerceptionService perceptionService;

  @BeforeEach
  void setUp() {
    perceptionService = new NearbyPerceptionService(spatialHash, directionCalculator, zoneManager);
    lenient().when(directionCalculator.getCardinalDirection(any(), any())).thenReturn("North");
  }

  @Test
  void testEntityVisibilityInSameZone() {
    Zone town = createZone("town", "Town Center", null, ZoneType.EXTERIOR);
    WorldObject anvil = createObject("anvil", "Anvil", "town", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(anvil));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(new Vector3(0, 0, 0), 10, "town", null);

    assertEquals(1, perception.elements().size());
    assertEquals("anvil", perception.elements().get(0).id());
    assertEquals(EntityType.OBJECT, perception.elements().get(0).type());
  }

  @Test
  void testEntityVisibilityFromExteriorToChildInterior() {
    Zone town = createZone("town", "Town Center", null, ZoneType.EXTERIOR);
    Zone blacksmith = createZone("blacksmith", "Blacksmith", "town", ZoneType.INTERIOR);
    WorldObject anvil = createObject("anvil", "Anvil", "blacksmith", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "blacksmith", blacksmith));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(anvil));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(new Vector3(0, 0, 0), 10, "town", null);

    assertTrue(perception.elements().isEmpty(), "Entities inside an INTERIOR should be hidden from the outside");
  }

  @Test
  void testZoneVisibilityHierarchically() {
    Zone town = createZone("town", "Town Center", null, ZoneType.EXTERIOR);
    Zone square = createZone("square", "Central Square", "town", ZoneType.EXTERIOR);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "square", square));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(square));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(new Vector3(0, 0, 0), 20, "town", null);

    assertEquals(1, perception.elements().size());
    assertEquals("square", perception.elements().get(0).id());
    assertEquals(EntityType.ZONE, perception.elements().get(0).type());
  }

  @Test
  void testEntityInParentExteriorIsHiddenFromInterior() {
    Zone town = createZone("town", "Town Center", null, ZoneType.EXTERIOR);
    Zone house = createZone("house", "Small House", "town", ZoneType.INTERIOR);
    WorldObject tree = createObject("tree", "Tree", "town", 10, 10);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "house", house));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(tree));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(
        new Vector3(0, 0, 0),
        20,
        "house",
        null
    );

    assertTrue(perception.elements().isEmpty(), "Objects outside should be hidden from an INTERIOR");
  }

  @Test
  void testParentExteriorZoneIsVisibleFromInteriorForNavigation() {
    Zone town = createZone("town", "Town Center", null, ZoneType.EXTERIOR);
    Zone house = createZone("house", "Small House", "town", ZoneType.INTERIOR);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("town", town, "house", house));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(town));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(
        new Vector3(0, 0, 0),
        20,
        "house",
        null
    );

    assertEquals(1, perception.elements().size());
    assertEquals("town", perception.elements().get(0).id());
  }

  @Test
  void testInteriorToSiblingInteriorVisibility() {
    Zone house = createZone("house", "House", null, ZoneType.INTERIOR);
    Zone kitchen = createZone("kitchen", "Kitchen", "house", ZoneType.INTERIOR);
    Zone living = createZone("living", "Living Room", "house", ZoneType.INTERIOR);
    WorldObject tv = createObject("tv", "TV", "living", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("house", house, "kitchen", kitchen, "living", living));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(tv, living));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(
        new Vector3(0, 0, 0),
        20,
        "kitchen",
        null
    );

    assertTrue(perception.elements().isEmpty(), "Objects and adjacent zones in another interior should be hidden");
  }

  @Test
  void testExteriorToSiblingExteriorVisibility() {
    Zone city = createZone("city", "City", null, ZoneType.EXTERIOR);
    Zone street = createZone("street", "Street", "city", ZoneType.EXTERIOR);
    Zone square = createZone("square", "Square", "city", ZoneType.EXTERIOR);
    WorldObject fountain = createObject("fountain", "Fountain", "square", 5, 5);

    when(zoneManager.getZoneMap()).thenReturn(Map.of("city", city, "street", street, "square", square));
    when(spatialHash.getNearbyEntities(any(), any(Double.class))).thenReturn(Set.<WorldElement>of(fountain, square));

    PeripheralPerception perception = perceptionService.getPeripheralPerception(
        new Vector3(0, 0, 0),
        20,
        "street",
        null
    );

    // Should see both the fountain and the square (they are both NearbyElements now)
    assertEquals(2, perception.elements().size(), "Both objects and zones in sibling exterior should be visible");
    assertTrue(perception.elements().stream().anyMatch(e -> e.id().equals("fountain")));
    assertTrue(perception.elements().stream().anyMatch(e -> e.id().equals("square")));
  }

  private Zone createZone(String id, String name, String parentId, ZoneType type) {
    return new Zone(id, name, parentId, type, "TEST", Set.of(), Set.of(), false, new Vector3(0, 0, 0), 0, 0);
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

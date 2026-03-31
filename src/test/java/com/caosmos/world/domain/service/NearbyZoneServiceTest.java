package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.caosmos.common.domain.model.world.NearbyZone;
import com.caosmos.common.domain.model.world.Vector3;
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
class NearbyZoneServiceTest {

  @Mock
  private ZoneManager zoneManager;
  @Mock
  private DirectionCalculator directionCalculator;

  private NearbyZoneService nearbyZoneService;

  @BeforeEach
  void setUp() {
    nearbyZoneService = new NearbyZoneService(zoneManager, directionCalculator);
    when(directionCalculator.getCardinalDirection(any(), any())).thenReturn("North");
  }

  @Test
  void testHierarchyFiltering() {
    // town (root) -> market (child)
    // town (root) -> park (child)
    // forest (root)
    Zone town = createZone("town", "Town Center", null, "EXTERIOR", 0, 0);
    Zone market = createZone("market", "Market", "town", "EXTERIOR", 10, 0);
    Zone park = createZone("park", "Park", "town", "EXTERIOR", 0, 10);
    Zone forest = createZone("forest", "Forest", null, "EXTERIOR", 100, 100);

    when(zoneManager.getAllZones()).thenReturn(List.of(town, market, park, forest));
    when(zoneManager.getZoneMap()).thenReturn(Map.of(
        "town", town,
        "market", market,
        "park", park,
        "forest", forest
    ));

    // Case 1: In 'town' (EXTERIOR root), should see children (market, park) AND root-sibling (forest)
    List<NearbyZone> nearTown = nearbyZoneService.getNearbyZones(new Vector3(0, 0, 0), "town", 200);
    assertEquals(3, nearTown.size());
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("market")));
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("park")));
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("forest"))); // Root peer - VISIBLE from EXTERIOR

    // Case 2: In 'market' (EXTERIOR child), should see parent (town) AND sibling (park)
    List<NearbyZone> nearMarket = nearbyZoneService.getNearbyZones(new Vector3(10, 0, 0), "market", 200);
    assertEquals(2, nearMarket.size());
    assertTrue(nearMarket.stream().anyMatch(z -> z.id().equals("town")));
    assertTrue(nearMarket.stream().anyMatch(z -> z.id().equals("park"))); // Sibling - VISIBLE from EXTERIOR
    assertFalse(nearMarket.stream().anyMatch(z -> z.id().equals("forest"))); // Not parent, not sibling, not child
  }

  @Test
  void testInteriorRule() {
    // town (EXTERIOR) -> blacksmith (INTERIOR) -> vault (INTERIOR)
    // park (EXTERIOR, sibling of blacksmith)
    Zone town = createZone("town", "Town Center", null, "EXTERIOR", 0, 0);
    Zone blacksmith = createZone("blacksmith", "Blacksmith", "town", "INTERIOR", 5, 5);
    Zone vault = createZone("vault", "Vault", "blacksmith", "INTERIOR", 5, 10);
    Zone park = createZone("park", "Park", "town", "EXTERIOR", 0, 10);

    when(zoneManager.getAllZones()).thenReturn(List.of(town, blacksmith, vault, park));
    when(zoneManager.getZoneMap()).thenReturn(Map.of(
        "town", town,
        "blacksmith", blacksmith,
        "vault", vault,
        "park", park
    ));

    // Case 1: Inside 'blacksmith' (INTERIOR). Should see Parent (town) but NOT other EXTERIOR (park)
    List<NearbyZone> nearBlacksmith = nearbyZoneService.getNearbyZones(new Vector3(5, 5, 0), "blacksmith", 200);
    assertEquals(2, nearBlacksmith.size());
    assertTrue(nearBlacksmith.stream().anyMatch(z -> z.id().equals("town"))); // Parent
    assertTrue(nearBlacksmith.stream().anyMatch(z -> z.id().equals("vault"))); // Child (Interior is OK)
    assertFalse(nearBlacksmith.stream().anyMatch(z -> z.id().equals("park"))); // EXTERIOR sibling - HIDDEN

    // Case 2: Inside 'vault' (Nested INTERIOR). Parent is blacksmith (INTERIOR). 
    // town is Grandparent (EXTERIOR) - should be hidden by hierarchy ±1 rule anyway.
    List<NearbyZone> nearVault = nearbyZoneService.getNearbyZones(new Vector3(5, 10, 0), "vault", 200);
    assertEquals(1, nearVault.size());
    assertTrue(nearVault.stream().anyMatch(z -> z.id().equals("blacksmith"))); // Parent
  }

  @Test
  void testRedundancyLimiting() {
    // 5 houses of the same type
    Zone town = createZone("town", "Town", null, "EXTERIOR", 0, 0);
    Zone h1 = createZone("h1", "House 1", "town", "INTERIOR", 10, 0, Set.of("HOUSE"));
    Zone h2 = createZone("h2", "House 2", "town", "INTERIOR", 20, 0, Set.of("HOUSE"));
    Zone h3 = createZone("h3", "House 3", "town", "INTERIOR", 30, 0, Set.of("HOUSE"));
    Zone h4 = createZone("h4", "House 4", "town", "INTERIOR", 40, 0, Set.of("HOUSE"));
    Zone h5 = createZone("h5", "House 5", "town", "INTERIOR", 50, 0, Set.of("HOUSE"));

    when(zoneManager.getAllZones()).thenReturn(List.of(town, h1, h2, h3, h4, h5));
    when(zoneManager.getZoneMap()).thenReturn(Map.of(
        "town", town, "h1", h1, "h2", h2, "h3", h3, "h4", h4, "h5", h5
    ));

    // From town, should see only 3 closest houses
    List<NearbyZone> nearTown = nearbyZoneService.getNearbyZones(new Vector3(0, 0, 0), "town", 200);
    // 3 houses + no other zones (itself excluded)
    // Wait, town is root, it perceives children h1..h5.
    // It should perceive 3 houses.
    assertEquals(3, nearTown.size());
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("h1")));
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("h2")));
    assertTrue(nearTown.stream().anyMatch(z -> z.id().equals("h3")));
    assertFalse(nearTown.stream().anyMatch(z -> z.id().equals("h4")));
  }

  private Zone createZone(String id, String name, String parentId, String type, double x, double z) {
    return createZone(id, name, parentId, type, x, z, Set.of());
  }

  private Zone createZone(
      String id,
      String name,
      String parentId,
      String type,
      double x,
      double z,
      Set<String> physical
  ) {
    return new Zone(id, name, parentId, type, "TEST", physical, Set.of(), false, new Vector3(x, 0, z), 2, 2);
  }
}

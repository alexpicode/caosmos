package com.caosmos.citizens.application.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.application.social.ConversationManager;
import com.caosmos.citizens.application.social.SocialHeuristicsEngine;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.Location;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.common.domain.model.world.WorldPerception;
import com.caosmos.common.domain.model.world.ZoneType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PerceptionMonitorTest {

  private PerceptionMonitor monitor;
  private Citizen citizen;
  private WorldDate date;
  private Environment environment;

  private TaskRegistry taskRegistry;
  private SocialHeuristicsEngine socialHeuristicsEngine;

  @Mock
  private WorldPort worldPort;

  @BeforeEach
  void setUp() {
    taskRegistry = new TaskRegistry();
    com.caosmos.citizens.application.social.ConversationConfigProperties config = new com.caosmos.citizens.application.social.ConversationConfigProperties(
        4);
    ConversationManager conversationManager = new ConversationManager(config);
    socialHeuristicsEngine = new SocialHeuristicsEngine(conversationManager);
    socialHeuristicsEngine.init();
    monitor = new PerceptionMonitor(taskRegistry, socialHeuristicsEngine);

    CitizenProfile profile = new CitizenProfile(
        new Identity("Tester", null, null, Collections.emptyList(), Collections.emptyMap()),
        new Status(100.0, 0.0, 100.0, 0.0),
        new CitizenProfile.BaseLocation(0, 0, 0),
        "Neutral",
        "manifest-1",
        0.0
    );
    citizen = new Citizen(UUID.randomUUID(), profile);
    date = new WorldDate(1, "Morning");
    environment = new Environment("Plain", List.of(), "High");
  }

  @Test
  void shouldDetectNoveltyWhenEnteringNewZone() {
    // Arrange
    WorldPerception perception = new WorldPerception(
        date,
        new Location("New Zone", "Building", "URBAN", "Hall", Set.of(), null, "zone-abc"),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Zone discovery"));
  }

  @Test
  void shouldDetectInterestingObject() {
    // Arrange
    NearbyElement interestingEntity = new NearbyElement(
        "Unique-ID",
        "Old Statue",
        "DECORATION",
        EntityType.OBJECT,
        null,
        5.0,
        "North",
        Set.of("INTERESTING"),
        null, null, null
    );
    citizen.enterZone("zone-1", "Square");
    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "Park", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(interestingEntity),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.isCritical(), "Interesting objects should not trigger a critical reflex");
  }

  @Test
  void shouldIgnoreNoveltyInFocusMode() {
    // Arrange
    WorldPerception perception = new WorldPerception(
        date,
        new Location("New Zone", "Building", "URBAN", "Hall", Set.of(), null, "zone-abc"),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(
        citizen,
        perception,
        false
    ); // focus mode: allowsRoutineInterruptions = false

    // Assert
    assertFalse(result.isCritical());
  }

  @Test
  void shouldIgnoreUnknownTerritory() {
    // Arrange: Start in a zone
    citizen.enterZone("zone-1", "Zone 1");

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Unknown Territory", "EXTERIOR", "WILDERNESS", "Open Area", Set.of(), null, null),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.isCritical(), "Entering Unknown Territory should not be critical");
    assertTrue(result.hasEnteredNewZone(), "Should still mark as zone changed to null");
  }

  @Test
  void shouldNotTriggerWhenStayingInUnknownTerritory() {
    // Arrange: Start in Unknown Territory
    citizen.enterZone(null, "Unknown Territory");

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Unknown Territory", "EXTERIOR", "WILDERNESS", "Open Area", Set.of(), null, null),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.hasEnteredNewZone(), "Should not detect change when staying in null");
  }

  @Test
  void shouldCompleteSearchWhenEnteringTargetCategoryZone() {
    // Arrange
    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "MINING",
            "Looking for gold",
            worldPort
        )
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Gold Mine", "INDUSTRIAL", "MINING", "Shaft", Set.of(), null, "zone-mine"),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Target found: mining"));
  }

  @Test
  void shouldNotTriggerSearchWhenStayingInTargetCategoryZone() {
    // Arrange: Pre-enter the zone
    citizen.enterZone("zone-mine", "Gold Mine");

    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "MINING",
            "Looking for gold",
            worldPort
        )
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Gold Mine", "INDUSTRIAL", "MINING", "Shaft", Set.of(), null, "zone-mine"),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.isCritical(), "Should not trigger when staying in same zone without new objects");
  }

  @Test
  void shouldCompleteSearchWhenSeeingTargetCategoryObject() {
    // Arrange
    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "TOOL",
            "Looking for a pickaxe",
            worldPort
        )
    );

    NearbyElement toolObject = new NearbyElement(
        "obj-1",
        "Iron Pickaxe",
        "TOOL",
        EntityType.OBJECT,
        null,
        2.0,
        "Forward",
        Set.of(),
        null, null, null
    );

    citizen.enterZone("zone-ws", "Workshop");
    WorldPerception perception = new WorldPerception(
        date,
        new Location("Workshop", "WORKSHOP", "WORKSHOP", "Table", Set.of(), null, "zone-ws"),
        environment,
        List.of(toolObject),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Target found: tool"));
  }

  @Test
  void shouldCompleteSearchWhenTargetZoneIsNearby() {
    // Arrange
    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "MARKET",
            "Looking for a market",
            worldPort
        )
    );

    NearbyElement nearbyMarket = new NearbyElement(
        "zone-mkt",
        "Town Market",
        "MARKET",
        EntityType.ZONE,
        ZoneType.EXTERIOR,
        50.0,
        "North-East",
        Collections.emptySet(),
        null, null, null
    );

    citizen.enterZone("zone-1", "Square");
    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "Park", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(nearbyMarket),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Target found: market"));
  }

  @Test
  void shouldPrioritizeSearchTargetOverNovelty() {
    // Arrange: Enter a NEW zone that is also the target category
    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "MINING",
            "Looking for gold",
            worldPort
        )
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Gold Mine", "INDUSTRIAL", "MINING", "Shaft", Set.of(), null, "zone-mine"),
        environment,
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Target found: mining"), "Search success should override Novelty");
    assertFalse(
        result.reason().toLowerCase().contains("discovery"),
        "Novelty should be suppressed when target is found"
    );
  }

  @Test
  void shouldPrioritizeSearchTargetOverInterestingObject() {
    // Arrange: See an interesting object AND the target object
    taskRegistry.register(
        citizen.getUuid(), new ExploreTask(
            new Vector3(1, 0, 0),
            "TOOL",
            "Looking for a pickaxe",
            worldPort
        )
    );

    NearbyElement interestingObject = new NearbyElement(
        "id-1",
        "Statue",
        "DECOR",
        EntityType.OBJECT,
        null,
        2.0,
        "F",
        Set.of("INTERESTING"),
        null, null, null
    );
    NearbyElement targetObject = new NearbyElement(
        "id-2",
        "Pickaxe",
        "TOOL",
        EntityType.OBJECT,
        null,
        5.0,
        "F",
        Set.of(),
        null,
        null,
        null
    );

    citizen.enterZone("zone-1", "Square");
    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "PARK", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(interestingObject, targetObject),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Target found: tool"), "Search success should override Distractions");
    assertFalse(
        result.reason().toLowerCase().contains("interest"),
        "Distractions should be suppressed when target is found"
    );
  }

  @Test
  void shouldNotTriggerThreatForNonHostileProximity() {
    // Arrange
    citizen.enterZone("zone-1", "Square"); // Ensure we don't trigger zone discovery

    NearbyElement closeCitizen = new NearbyElement(
        "id-1",
        "Greg",
        "HUMAN",
        EntityType.CITIZEN,
        null,
        0.5, // Well below proximity threshold
        "F",
        Set.of(),
        null, null, null
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "PARK", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(closeCitizen),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.isCritical(), "Close non-hostile citizen should not be a threat. Got: " + result.reason());
  }

  @Test
  void shouldTriggerThreatForHostileEntity() {
    // Arrange
    NearbyElement hostileEntity = new NearbyElement(
        "id-1",
        "Enemy",
        "MONSTER",
        EntityType.CITIZEN,
        null,
        10.0, // Far away
        "F",
        Set.of("HOSTILE"),
        null, null, null
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "PARK", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(hostileEntity),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Threat detected: Enemy"));
  }

  @Test
  void shouldTriggerNearbyObjectForCloseObject() {
    // Arrange
    citizen.enterZone("zone-1", "Square");

    NearbyElement closeObject = new NearbyElement(
        "id-obj",
        "Iron Ore",
        "RESOURCE",
        EntityType.OBJECT,
        null,
        0.5, // Close
        "F",
        Set.of(),
        null, null, null
    );

    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "PARK", "NATURE", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(closeObject),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.isCritical(), "Close objects should not trigger a critical reflex");
  }
}

package com.caosmos.citizens.application.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.PerceptionEvaluation;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.Location;
import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PerceptionMonitorTest {

  private PerceptionMonitor monitor;
  private Citizen citizen;
  private WorldDate date;
  private Environment environment;

  private TaskRegistry taskRegistry;

  @BeforeEach
  void setUp() {
    taskRegistry = new TaskRegistry();
    monitor = new PerceptionMonitor(taskRegistry);
    CitizenProfile profile = new CitizenProfile(
        new Identity("Tester", null, null, Collections.emptyList(), Collections.emptyMap()),
        new Status(100.0, 0.0, 100.0, 0.0),
        new CitizenProfile.BaseLocation(0, 0, 0),
        "Neutral",
        "manifest-1"
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
        new Location("New Zone", "Building", "TEST", "Hall", Set.of(), null, "zone-abc"),
        environment,
        Collections.emptyList(),
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
    NearbyEntity interestingEntity = new NearbyEntity(
        "Unique-ID",
        "Old Statue",
        "DECORATION",
        5.0,
        "North",
        Set.of("INTERESTING")
    );
    WorldPerception perception = new WorldPerception(
        date,
        new Location("Square", "Park", "TEST", "Center", Set.of(), null, "zone-1"),
        environment,
        List.of(interestingEntity),
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertTrue(result.isCritical());
    assertTrue(result.reason().contains("Object of interest"));
  }

  @Test
  void shouldIgnoreNoveltyInFocusMode() {
    // Arrange
    WorldPerception perception = new WorldPerception(
        date,
        new Location("New Zone", "Building", "TEST", "Hall", Set.of(), null, "zone-abc"),
        environment,
        Collections.emptyList(),
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
        new Location("Unknown Territory", "EXTERIOR", "TEST", "Open Area", Set.of(), null, null),
        environment,
        Collections.emptyList(),
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
        new Location("Unknown Territory", "EXTERIOR", "TEST", "Open Area", Set.of(), null, null),
        environment,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptySet()
    );

    // Act
    PerceptionEvaluation result = monitor.evaluate(citizen, perception, true);

    // Assert
    assertFalse(result.hasEnteredNewZone(), "Should not detect change when staying in null");
  }
}

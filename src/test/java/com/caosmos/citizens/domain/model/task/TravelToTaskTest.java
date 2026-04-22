package com.caosmos.citizens.domain.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.citizens.domain.task.TravelToTask;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.world.CollisionResult;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TravelToTaskTest {

  private Citizen citizen;
  private TravelToTask task;

  @Mock
  private WorldPort worldPort;

  @BeforeEach
  void setUp() {
    UUID uuid = UUID.randomUUID();
    Status initialStatus = new Status(100.0, 0.0, 100.0, 0.0);
    Identity identity = new Identity("Test Citizen", null, null, Collections.emptyList(), Collections.emptyMap());
    // Provide a non-null base location to ensure citizen has a position
    CitizenProfile.BaseLocation base = new CitizenProfile.BaseLocation(0, 0, 0);
    CitizenProfile profile = new CitizenProfile(identity, initialStatus, base, "Normal", "manifest-1", 0.0);

    citizen = new Citizen(uuid, profile);

    // Default mock behavior: no collision, just return target position
    lenient().when(worldPort.validateMovement(any(), any(), any()))
        .thenAnswer(invocation -> new CollisionResult(invocation.getArgument(1), false));

    task = new TravelToTask(new Vector3(100, 0, 100), "target-1", worldPort);
  }

  @Test
  void testMovementConsumesEnergyAndHunger() {
    double dt = 3600.0; // 1 hour
    task.executeOnTick(citizen, null, dt, 1.0);

    Status status = citizen.getPerception().status();

    // Navigation energy cost: -2.0/h
    assertEquals(100.0 - 2.0, status.energy(), 0.01);
    // Navigation hunger increase: +2.0/h
    assertEquals(0.0 + 2.0, status.hunger(), 0.01);
  }

  @Test
  void testFatigueReducesSpeed() {
    // Force extreme fatigue
    citizen.biology().decreaseEnergy(90.0); // Energy = 10.0 (< 15.0 EXTREME_FATIGUE)

    Vector3 startPos = citizen.getPosition();
    task.executeOnTick(citizen, null, 10.0, 1.0); // Move for 10 seconds at 1.0 m/s

    Vector3 endPos = citizen.getPosition();
    double movedDistance = startPos.distanceTo(endPos);

    // Normal distance should be 10.0m. Reduced should be 10.0 * 0.5 = 5.0m
    assertEquals(5.0, movedDistance, 0.1);
  }
}

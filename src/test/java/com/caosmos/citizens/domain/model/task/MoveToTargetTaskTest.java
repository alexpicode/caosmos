package com.caosmos.citizens.domain.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MoveToTargetTaskTest {

  private Citizen citizen;
  private MoveToTargetTask task;

  @BeforeEach
  void setUp() {
    UUID uuid = UUID.randomUUID();
    Status initialStatus = new Status(100.0, 0.0, 100.0, 0.0);
    Identity identity = new Identity("Test Citizen", Collections.emptyList(), Collections.emptyMap());
    // Provide a non-null base location to ensure citizen has a position
    CitizenProfile.BaseLocation base = new CitizenProfile.BaseLocation(0, 0, 0);
    CitizenProfile profile = new CitizenProfile(identity, initialStatus, base, "Normal", "manifest-1");

    citizen = new Citizen(uuid, profile);
    task = new MoveToTargetTask(new Vector3(100, 0, 100), "target-1");
  }

  @Test
  void testMovementConsumesEnergyAndHunger() {
    double dt = 3600.0; // 1 hour
    task.executeOnTick(citizen, dt, 1.0);

    Status status = citizen.getPerception().status();

    // Navigation energy cost: -1.5/h
    assertEquals(100.0 - 1.5, status.energy(), 0.01);
    // Navigation hunger cost: +1.0/h
    assertEquals(1.0, status.hunger(), 0.01);
  }

  @Test
  void testFatigueReducesSpeed() {
    // Force extreme fatigue
    citizen.consumeEnergy(90.0); // Energy = 10.0 (< 15.0 EXTREME_FATIGUE)

    Vector3 startPos = citizen.getPosition();
    task.executeOnTick(citizen, 10.0, 1.0); // Move for 10 seconds at 1.0 m/s

    Vector3 endPos = citizen.getPosition();
    double movedDistance = startPos.distanceTo(endPos);

    // Normal distance should be 10.0m. Reduced should be 10.0 * 0.5 = 5.0m
    assertEquals(5.0, movedDistance, 0.1);
  }
}

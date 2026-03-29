package com.caosmos.citizens.domain.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.citizens.domain.task.SleepTask;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SleepTaskTest {

  private Citizen citizen;
  private SleepTask task;

  @BeforeEach
  void setUp() {
    UUID uuid = UUID.randomUUID();
    Status initialStatus = new Status(100.0, 50.0, 20.0, 50.0);
    Identity identity = new Identity("Test Citizen", null, null, Collections.emptyList(), Collections.emptyMap());
    CitizenProfile profile = new CitizenProfile(identity, initialStatus, null, "Normal", "manifest-1");

    citizen = new Citizen(uuid, profile);
    task = new SleepTask();
  }

  @Test
  void testSleepRecoversEnergyAndReducesStress() {
    double dt = 3600.0; // 1 hour
    task.executeOnTick(citizen, dt, 0.0);

    Status status = citizen.getPerception().status();

    // Energy recovery rate: 10/h
    assertEquals(20.0 + 10.0, status.energy(), 0.01);
    // Stress reduction rate: -5/h
    assertEquals(50.0 - 5.0, status.stress(), 0.01);
    // Hunger increase rate: 0.2/h
    assertEquals(50.0 + 0.2, status.hunger(), 0.01);
  }

  @Test
  void testSleepCompletesAtFullEnergy() {
    // Almost full energy
    citizen.biology().increaseEnergy(79.5); // Energy = 99.5

    var result = task.executeOnTick(citizen, 360.0, 0.0); // 6 mins = 1.0 energy recovery

    assertTrue(result.completed());
    assertTrue(citizen.getPerception().status().energy() >= 100.0);
  }
}

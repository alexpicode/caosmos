package com.caosmos.citizens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Status;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PhysiologicalMotorTest {

  private PhysiologicalMotor motor;
  private Citizen citizen;

  @BeforeEach
  void setUp() {
    motor = new PhysiologicalMotor();
    CitizenProfile profile = mock(CitizenProfile.class);
    when(profile.status()).thenReturn(new Status(100, 0, 100, 0));
    citizen = new Citizen(UUID.randomUUID(), profile);
  }

  @Test
  void testPassiveMetabolismAppliesCosts() {
    double dt = 3600.0; // 1 hour
    motor.applyPassiveMetabolism(citizen, dt);

    Status status = citizen.getPerception().status();

    // Hunger should increase by PASSIVE_HUNGER_RATE
    assertEquals(PhysiologicalThresholds.PASSIVE_HUNGER_RATE, status.hunger(), 0.01);
    // Energy should decrease by PASSIVE_ENERGY_DECAY_RATE
    assertEquals(100.0 - PhysiologicalThresholds.PASSIVE_ENERGY_DECAY_RATE, status.energy(), 0.01);
  }

  @Test
  void testStarvationDrainsVitality() {
    // Force high hunger
    for (int i = 0; i < 200; i++) {
      citizen.increaseHunger(1.0);
    }

    double dt = 3600.0; // 1 hour
    motor.applyPassiveMetabolism(citizen, dt);

    Status status = citizen.getPerception().status();
    assertTrue(status.vitality() < 100.0, "Vitality should have decreased due to starvation");
  }

  @Test
  void testCriticalThresholdExhaustion() {
    // Force low energy
    citizen.consumeEnergy(96.0); // Energy = 4.0 (< 5.0 collapse)

    var reflex = motor.evaluateCriticalThresholds(citizen);

    assertTrue(reflex.isPresent());
    assertEquals("SLEEP", reflex.get().forcedActionType());
  }
}

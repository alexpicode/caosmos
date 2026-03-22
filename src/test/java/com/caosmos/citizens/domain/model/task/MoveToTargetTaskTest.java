package com.caosmos.citizens.domain.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MoveToTargetTaskTest {

  private CitizenProfile createMockProfile() {
    CitizenProfile profile = mock(CitizenProfile.class);
    Status status = new Status(100, 0, 100, 0);
    when(profile.status()).thenReturn(status);
    return profile;
  }

  @Test
  void testMovementWithRealCitizen() {
    // Given
    Vector3 start = new Vector3(0, 0, 0);
    Vector3 target = new Vector3(10, 0, 0);
    Citizen citizen = new Citizen(UUID.randomUUID(), createMockProfile());
    citizen.getCurrentState().setPosition(start);

    MoveToTargetTask task = new MoveToTargetTask(target, "test-target");
    double dt = 2.0; // 2 seconds -> should move 2.8m

    // When
    ActiveTask result = task.executeOnTick(citizen, dt, 1.4);

    // Then
    assertEquals(2.8, citizen.getPosition().x(), 0.001);
    assertFalse(result.completed());
  }

  @Test
  void testArrival() {
    // Given
    Vector3 start = new Vector3(9.9, 0, 0);
    Vector3 target = new Vector3(10, 0, 0);
    Citizen citizen = new Citizen(UUID.randomUUID(), createMockProfile());
    citizen.getCurrentState().setPosition(start);

    MoveToTargetTask task = new MoveToTargetTask(target, "test-target");
    double dt = 1.0; // 1 second -> would move 1.4m, which is more than 0.1m

    // When
    ActiveTask result = task.executeOnTick(citizen, dt, 1.4);

    // Then
    assertEquals(10.0, citizen.getPosition().x(), 0.001);
    assertTrue(result.completed());
  }
}

package com.caosmos.citizens.domain.model.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExploreTaskTest {

  private Citizen citizen;
  private ExploreTask task;

  @BeforeEach
  void setUp() {
    UUID uuid = UUID.randomUUID();
    Status initialStatus = new Status(100.0, 0.0, 100.0, 0.0);
    Identity identity = new Identity("Explorer", null, null, Collections.emptyList(), Collections.emptyMap());
    CitizenProfile.BaseLocation base = new CitizenProfile.BaseLocation(0, 0, 0);
    CitizenProfile profile = new CitizenProfile(identity, initialStatus, base, "Adventurous", "manifest-1");

    citizen = new Citizen(uuid, profile);
    // Explore North (0, 0, 1)
    task = new ExploreTask(new Vector3(0, 0, 1), null, null);
  }

  @Test
  void shouldInitializeTargetOnFirstTick() {
    task.executeOnTick(citizen, 1.0, 1.0);
    // Should move north
    assertTrue(citizen.getPosition().z() > 0);
  }

  @Test
  void shouldCompleteWhenDistanceReached() {
    // 1. Initial tick to set startPos at current position (0,0,0)
    task.executeOnTick(citizen, 1.0, 1.0);

    // 2. Set position near the 50m limit
    citizen.getCurrentState().setPosition(new Vector3(0, 0, 49.9));

    // 3. One more tick should complete it
    ActiveTask result = task.executeOnTick(citizen, 1.0, 1.0);

    assertTrue(result.completed());
    assertEquals("Exploration reached limit", result.goal());
  }

  @Test
  void shouldAllowRoutineInterruptions() {
    assertTrue(task.allowsRoutineInterruptions());
  }
}

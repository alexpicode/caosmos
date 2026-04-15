package com.caosmos.citizens.application.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.common.domain.contracts.WorldRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CitizenRegistryTest {

  private CitizenRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new CitizenRegistry(Mockito.mock(WorldRegistry.class));
  }

  @Test
  void shouldRegisterAndGetCitizenByUuid() {
    UUID uuid = UUID.randomUUID();

    Status status = new Status(100, 0, 100, 0);
    CitizenProfile profile = new CitizenProfile(null, status, null, "Friendly", "test-manifest", 0.0);
    Citizen citizen = new Citizen(uuid, profile);

    registry.register(uuid, citizen);
    Optional<Citizen> retrieved = registry.get(uuid);

    assertTrue(retrieved.isPresent());
    assertEquals(citizen, retrieved.get());
    assertEquals(uuid, retrieved.get().getUuid());
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    Optional<Citizen> retrieved = registry.get(UUID.randomUUID());
    assertTrue(retrieved.isEmpty());
  }
}

package com.caosmos.common.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SanityCheckerTest {

  @Mock
  private WorldPort worldPort;

  @Mock
  private com.caosmos.common.domain.contracts.CitizenPort citizenPort;

  private SanityChecker sanityChecker;
  private final UUID citizenId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    sanityChecker = new SanityChecker();
  }

  @Test
  void testPickupOwnedButUnlockedObject_ShouldNotBeBlocked() {
    String targetId = "sword_1";
    ActionIntent intent = new ActionIntent(
        citizenId,
        "PICKUP",
        targetId,
        Set.of(), Set.of(), Set.of(),
        new Vector3(0, 0, 0), new Vector3(0, 0, 0)
    );

    WorldElement sword = mock(WorldElement.class);
    lenient().when(sword.getType()).thenReturn(EntityType.OBJECT);
    lenient().when(sword.getTags()).thenReturn(Set.of("owner:someone_else")); // Owned but NOT locked
    lenient().when(worldPort.getElement(targetId)).thenReturn(Optional.of(sword));
    lenient().when(worldPort.getObject(targetId)).thenReturn(Optional.of(sword));
    lenient().when(citizenPort.isNear(any(), any(), anyDouble())).thenReturn(true);

    Optional<String> result = sanityChecker.validate(intent, citizenPort, worldPort);

    assertTrue(result.isEmpty(), "Ownership without LOCKED should NOT block PICKUP");
  }

  @Test
  void testUseOwnedAndLockedObject_ShouldBeBlocked() {
    String targetId = "chest_1";
    ActionIntent intent = new ActionIntent(
        citizenId,
        "USE",
        targetId,
        Set.of(), Set.of(), Set.of(),
        new Vector3(0, 0, 0), new Vector3(0, 0, 0)
    );

    WorldElement chest = mock(WorldElement.class);
    lenient().when(chest.getType()).thenReturn(EntityType.OBJECT);
    // Owned AND Locked
    lenient().when(chest.getTags()).thenReturn(Set.of("owner:someone_else", WorldConstants.TAG_LOCKED));
    lenient().when(worldPort.getElement(targetId)).thenReturn(Optional.of(chest));
    lenient().when(worldPort.getObject(targetId)).thenReturn(Optional.of(chest));
    lenient().when(citizenPort.isNear(any(), any(), anyDouble())).thenReturn(true);
    lenient().when(worldPort.getObjectTags(any())).thenReturn(Set.of());

    Optional<String> result = sanityChecker.validate(intent, citizenPort, worldPort);

    assertFalse(result.isEmpty(), "Locked objects should block USE for non-owners");
    assertEquals("This is locked.", result.get());
  }
}

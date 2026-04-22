package com.caosmos.actions.application.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimPropertyActionHandlerTest {

  @Mock
  private WorldPort worldPort;
  @Mock
  private CitizenPort citizenPort;

  private ClaimPropertyActionHandler handler;
  private final UUID citizenId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    handler = new ClaimPropertyActionHandler(worldPort, citizenPort);
  }

  @Test
  void testClaimUnownedZone_Success() {
    String zoneId = "shop_1";
    ActionRequest request = new ActionRequest("CLAIM", "reasoning", Map.of("targetId", zoneId));

    WorldElement zone = mock(WorldElement.class);
    when(zone.getType()).thenReturn(EntityType.ZONE);
    when(zone.getTags()).thenReturn(Set.of(WorldConstants.TAG_UNOWNED));

    when(worldPort.getElement(zoneId)).thenReturn(Optional.of(zone));
    when(citizenPort.isNear(citizenId, zoneId, ActionThresholds.PROXIMITY_USE)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    verify(worldPort).removeTag(zoneId, WorldConstants.TAG_UNOWNED);
    verify(worldPort).addTag(eq(zoneId), contains(WorldConstants.PREFIX_OWNER));
  }

  @Test
  void testClaimOccupiedZone_Failure() {
    String zoneId = "house_1";
    ActionRequest request = new ActionRequest("CLAIM", "reasoning", Map.of("targetId", zoneId));

    WorldElement zone = mock(WorldElement.class);
    when(zone.getType()).thenReturn(EntityType.ZONE);
    when(zone.getTags()).thenReturn(Set.of("owner:someone_else"));

    when(worldPort.getElement(zoneId)).thenReturn(Optional.of(zone));
    when(citizenPort.isNear(citizenId, zoneId, ActionThresholds.PROXIMITY_USE)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertFalse(result.success());
    assertEquals("This property is already owned.", result.message());
  }

  @Test
  void testClaimWorkstation_Success() {
    String anvilId = "anvil_1";
    ActionRequest request = new ActionRequest("CLAIM", "reasoning", Map.of("targetId", anvilId));

    WorldElement anvil = mock(WorldElement.class);
    when(anvil.getType()).thenReturn(EntityType.OBJECT);
    when(anvil.getTags()).thenReturn(Set.of(WorldConstants.TAG_WORKSTATION));

    when(worldPort.getElement(anvilId)).thenReturn(Optional.of(anvil));
    when(citizenPort.isNear(citizenId, anvilId, ActionThresholds.PROXIMITY_USE)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    verify(worldPort).addTag(eq(anvilId), contains(WorldConstants.PREFIX_OWNER + citizenId));
  }
}

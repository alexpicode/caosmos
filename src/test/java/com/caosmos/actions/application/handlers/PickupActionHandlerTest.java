package com.caosmos.actions.application.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.EconomyPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PickupActionHandlerTest {

  private WorldPort worldPort;
  private CitizenPort citizenPort;
  private EconomyPort economyPort;
  private PickupActionHandler handler;
  private UUID citizenId;

  @BeforeEach
  void setUp() {
    worldPort = mock(WorldPort.class);
    citizenPort = mock(CitizenPort.class);
    economyPort = mock(EconomyPort.class);
    handler = new PickupActionHandler(worldPort, citizenPort, economyPort);
    citizenId = UUID.randomUUID();
  }

  @Test
  void testPickupFailsWhenFar() {
    String targetId = "rock1";
    ActionRequest request = new ActionRequest("PICKUP", null, Map.of("targetId", targetId));

    when(citizenPort.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_PICKUP)).thenReturn(false);

    ActionResult result = handler.execute(citizenId, request);

    assertFalse(result.success());
    assertTrue(result.message().contains("too far"));
    verify(worldPort, never()).removeObject(anyString());
  }

  @Test
  void testPickupSucceedsWhenNear() {
    String targetId = "rock1";
    ActionRequest request = new ActionRequest("PICKUP", null, Map.of("targetId", targetId));
    ItemData item = new ItemData(targetId, "Rock", Collections.emptyList(), "RESOURCE", 0.1, null, null, null);

    when(citizenPort.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_PICKUP)).thenReturn(true);
    when(worldPort.removeObject(targetId)).thenReturn(item);
    when(citizenPort.addToInventory(citizenId, item)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    assertEquals("Picked up Rock", result.message());
  }

  @Test
  void testCurrencyPickupInterceptor() {
    String targetId = "bag1";
    ActionRequest request = new ActionRequest("PICKUP", null, Map.of("targetId", targetId));
    ItemData coinBag = new ItemData(targetId, "Coin Bag", List.of("coin_container"), "COIN", 0.08, null, null, 50.0);

    when(citizenPort.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_PICKUP)).thenReturn(true);
    when(worldPort.removeObject(targetId)).thenReturn(coinBag);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    assertTrue(result.message().contains("50.0 coins"));
    verify(economyPort).addCoins(citizenId, 50.0);
    verify(citizenPort, never()).addToInventory(eq(citizenId), any(ItemData.class));
  }
}

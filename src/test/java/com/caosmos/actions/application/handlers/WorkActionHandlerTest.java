package com.caosmos.actions.application.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkActionHandlerTest {

  private CitizenPort citizenPort;
  private WorkActionHandler handler;
  private UUID citizenId;

  @BeforeEach
  void setUp() {
    citizenPort = mock(CitizenPort.class);
    handler = new WorkActionHandler(citizenPort);
    citizenId = UUID.randomUUID();
  }

  @Test
  void testWorkFailsWhenNotInZone() {
    ActionRequest request = new ActionRequest("WORK", null, Map.of("workplaceType", "mine"));

    when(citizenPort.isInZoneWithTag(citizenId, ActionThresholds.TAG_MINING)).thenReturn(false);

    ActionResult result = handler.execute(citizenId, request);

    assertFalse(result.success());
    assertTrue(result.message().contains("not at an appropriate mine workplace"));
    verify(citizenPort, never()).assignWorkTask(any(), anyString());
  }

  @Test
  void testWorkSucceedsInZoneWithTool() {
    ActionRequest request = new ActionRequest("WORK", null, Map.of("workplaceType", "mine"));

    when(citizenPort.isInZoneWithTag(citizenId, ActionThresholds.TAG_MINING)).thenReturn(true);
    when(citizenPort.isItemEquippedWithTag(citizenId, ActionThresholds.ITEM_TAG_MINING)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    verify(citizenPort).assignWorkTask(citizenId, "mine");
  }

  @Test
  void testWorkFailsInZoneWithoutTool() {
    ActionRequest request = new ActionRequest("WORK", null, Map.of("workplaceType", "mine"));

    when(citizenPort.isInZoneWithTag(citizenId, ActionThresholds.TAG_MINING)).thenReturn(true);
    when(citizenPort.isItemEquippedWithTag(citizenId, ActionThresholds.ITEM_TAG_MINING)).thenReturn(false);

    ActionResult result = handler.execute(citizenId, request);

    assertFalse(result.success());
    assertTrue(result.message().contains("You need a tool with the tag 'mining' to work here."));
    verify(citizenPort, never()).assignWorkTask(any(), anyString());
  }

  @Test
  void testWorkSucceedsInZoneNoToolRequired() {
    ActionRequest request = new ActionRequest("WORK", null, Map.of("workplaceType", "shop"));

    when(citizenPort.isInZoneWithTag(citizenId, ActionThresholds.TAG_COMMERCE)).thenReturn(true);

    ActionResult result = handler.execute(citizenId, request);

    assertTrue(result.success());
    verify(citizenPort).assignWorkTask(citizenId, "shop");
  }
}

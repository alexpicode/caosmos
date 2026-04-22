package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.EconomyPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DropActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;
  private final EconomyPort economyService;

  @Override
  public String getActionType() {
    return "DROP";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for DROP", getActionType());
    }

    if ("MONEY".equalsIgnoreCase(targetId)) {
      Object amountObj = request.parameters().get("amount");
      double amount = 0;
      if (amountObj instanceof Number n) {
        amount = n.doubleValue();
      } else if (amountObj instanceof String s) {
        try {
          amount = Double.parseDouble(s);
        } catch (NumberFormatException e) {
          return ActionResult.failure("Invalid amount for money drop", getActionType());
        }
      }

      if (amount <= 0) {
        return ActionResult.failure("Amount must be positive", getActionType());
      }

      if (economyService.subtractCoins(citizenId, amount)) {
        Vector3 citizenPos = citizenService.getPosition(citizenId);
        ItemData coinBag = new ItemData(
            "coin_bag_" + UUID.randomUUID().toString().substring(0, 8),
            "Coin Bag",
            Set.of("coin_container", "valuable"),
            "COIN",
            "A small bag with " + amount + " clinking coins inside.",
            0.08,
            null, // width
            null, // length
            amount
        );
        String currentZoneId = citizenService.getCurrentZoneId(citizenId);
        worldService.spawnObject(citizenPos, currentZoneId, coinBag);
        citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_DROP);
        return ActionResult.success("Dropped " + amount + " coins", getActionType());
      } else {
        return ActionResult.failure("Insufficient coins to drop " + amount, getActionType());
      }
    }

    ItemData item = citizenService.removeFromInventory(citizenId, targetId);

    if (item != null) {
      Vector3 citizenPos = citizenService.getPosition(citizenId);
      String currentZoneId = citizenService.getCurrentZoneId(citizenId);
      worldService.spawnObject(citizenPos, currentZoneId, item);
      citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_DROP);
      return ActionResult.success("Dropped " + item.name(), getActionType());
    } else {
      return ActionResult.failure("Item " + targetId + " not found in inventory", getActionType());
    }
  }
}

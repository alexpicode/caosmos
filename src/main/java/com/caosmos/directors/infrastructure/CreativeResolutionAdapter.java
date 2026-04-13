package com.caosmos.directors.infrastructure;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.CreativeResolutionPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.service.SanityChecker;
import com.caosmos.directors.application.DirectorArbitrator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreativeResolutionAdapter implements CreativeResolutionPort {

  private final SanityChecker sanityChecker;
  private final DirectorArbitrator directorArbitrator;
  private final WorldPort worldPort;
  private final CitizenPort citizenPort;

  @Override
  public ActionResult resolve(UUID citizenId, ActionRequest request) {
    // 1. Resolve Target parameters (we must have a target to perform USE)
    String targetId = (String) request.parameters().get("targetId");
    if (targetId == null) {
      return ActionResult.failure("Target ID is required", request.type());
    }

    Vector3 citizenPosition = citizenPort.getPosition(citizenId);
    Vector3 targetPosition = worldPort.getObjectPosition(targetId)
        .orElse(citizenPosition); // fallback position if not found

    // 2. Fetch all related entity tags required to build the semantic context
    // toolTags: the specific tags of the active tool used. 
    // If not specified, the citizen is using their bare hands (empty tags).
    String toolId = (String) request.parameters().get("toolId");
    Set<String> toolTags;
    if (toolId != null) {
      toolTags = citizenPort.getEquippedItemTags(citizenId, toolId);
    } else {
      toolTags = Collections.emptySet();
    }

    Set<String> targetTags = worldPort.getObjectTags(targetId);

    // 3. Obtain Weather/Environment modifiers
    Set<String> envTags = worldPort.getNormalizedEnvironmentTags().stream()
        .map(EnvironmentImpactTag::name)
        .collect(Collectors.toSet());

    // 4. Build a comprehensive record encoding the citizen's physical intent
    ActionIntent intent = new ActionIntent(
        citizenId,
        request.type(),
        targetId,
        toolTags,
        targetTags,
        envTags,
        citizenPosition,
        targetPosition
    );

    // 5. Hard validation constraint (Sanity Check): Proximity and real existence check before bothering the AI
    Optional<String> error = sanityChecker.validate(intent, citizenPort, worldPort);
    if (error.isPresent()) {
      return ActionResult.failure(error.get(), request.type());
    }

    String targetName = worldPort.getObject(targetId).map(e -> e.getName()).orElse(targetId);
    String targetCategory = worldPort.getObject(targetId).map(e -> e.getCategory()).orElse("object");

    // 6. Invoke the Director (Arbitrator) for a final physics veredict
    var result = directorArbitrator.resolve(intent, targetName, targetCategory);

    if (result.success()) {
      // 7. Apply deterministic physical mutations given by the AI
      if (result.mutations() != null) {
        for (var mut : result.mutations()) {
          if ("ADD_TAG".equals(mut.mutationType())) {
            worldPort.updateObjectTag(mut.targetId(), mut.value());
          }
        }
      }
      // 8. Cost of execution
      //TODO Add a configuration for the cost of execution
      citizenPort.consumeEnergy(citizenId, 4.0); // ENERGY_COST_USE

      // 9. Inject AI generated narration as a system event in the Action Result changes map
      Map<String, Object> changes = new HashMap<>();
      changes.put("narration", result.narration());
      return new ActionResult(true, result.narration(), request.type(), changes);
    } else {
      // Forward AI's failure narration (e.g. "The torch cannot ignite the wet wood")
      return ActionResult.failure(result.narration(), request.type());
    }
  }
}

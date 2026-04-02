package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.function.Predicate;

public interface WorldPerceptionProvider {

  WorldPerception getPerceptionAt(Vector3 position, String currentZoneId, Predicate<WorldElement> filter);
}

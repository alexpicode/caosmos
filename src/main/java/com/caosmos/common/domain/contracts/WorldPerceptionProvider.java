package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.function.Predicate;

public interface WorldPerceptionProvider {

  WorldPerception getPerceptionAt(Vector3 position, Predicate<WorldEntity> filter);
}

package com.caosmos.world.application.config;

import com.caosmos.world.domain.model.WorldObject;
import java.util.List;

public record WorldObjectsConfig(
    List<WorldObject> worldObjects
) {

}

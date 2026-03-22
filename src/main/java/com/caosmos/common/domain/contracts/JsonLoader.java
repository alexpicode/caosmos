package com.caosmos.common.domain.contracts;

import org.springframework.core.io.Resource;

public interface JsonLoader {

  <T> T load(Resource resource, Class<T> configClass);
}

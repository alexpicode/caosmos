package com.caosmos.common.domain.model.manifest;

import java.util.Map;

/**
 * Universal contract representing the raw data of an agent's manifest.
 */
public record AgentManifest(
    String fileName,
    Map<String, Object> metadata,
    String personality
) {

}

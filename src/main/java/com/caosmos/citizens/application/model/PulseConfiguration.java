package com.caosmos.citizens.application.model;

import org.springframework.core.io.Resource;

/**
 * Configuration object for citizen pulse behavior. Encapsulates all pulse-related configuration parameters.
 */
public record PulseConfiguration(
    int pulseFrequencySeconds,
    Resource systemPromptResource,
    Resource userPromptResource
) {

}

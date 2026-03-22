package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record ReflexResult(
    boolean critical,
    String reason,
    List<String> informativeEvents
) {

}

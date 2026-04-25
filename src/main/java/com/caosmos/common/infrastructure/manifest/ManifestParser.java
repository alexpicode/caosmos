package com.caosmos.common.infrastructure.manifest;

import com.caosmos.common.domain.model.manifest.AgentManifest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Parser for agent manifests with YAML frontmatter and Markdown body.
 */
@Component
@Slf4j
public class ManifestParser {

  private final ObjectMapper yamlMapper;

  public ManifestParser() {
    this.yamlMapper = new ObjectMapper(new YAMLFactory());
  }

  public AgentManifest parseManifest(Path manifestPath) throws IOException {
    String content = Files.readString(manifestPath);
    return parseManifestContent(content, manifestPath.getFileName().toString());
  }

  public AgentManifest parseManifestFromString(String content, String fileName) throws IOException {
    return parseManifestContent(content, fileName);
  }

  private AgentManifest parseManifestContent(String content, String fileName) throws IOException {
    // Split frontmatter and body
    String[] parts = content.split("(?m)^---\\s*$", 3);

    // Support for files that might start with --- or not
    int yamlIndex = (parts.length > 2) ? 1 : 0;
    int bodyIndex = (parts.length > 2) ? 2 : 1;

    if (parts.length < 2) {
      throw new IOException("Invalid manifest format");
    }

    // Parse YAML frontmatter
    Map<String, Object> metadata = yamlMapper.readValue(parts[yamlIndex], new TypeReference<>() {});

    return new AgentManifest(
        fileName,
        metadata,
        parts[bodyIndex].trim()
    );
  }
}

package com.caosmos.common.infrastructure.manifest;

import com.caosmos.common.infrastructure.config.CaosmosResourceProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * File system resolver for manifest files implementing overlay strategy. Responsibility: Navigate file system and
 * resolve absolute paths with priority rules.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ManifestFileSystemResolver {

  private final CaosmosResourceProperties resourceProperties;

  /**
   * Finds manifest path using overlay system (external first, then internal).
   *
   * @param manifestName Name of the manifest file
   * @return Path to the manifest file, or null if not found
   */
  public Path findManifestPath(String manifestName) {
    try {
      if (resourceProperties.citizens() == null) {
        return null;
      }

      Path baseDir = resourceProperties.citizens().getFile().toPath();
      Path manifestPath = baseDir.resolve(manifestName);

      if (Files.exists(manifestPath)) {
        log.debug("[RESOLVER] Found manifest: {}", manifestName);
        return manifestPath.toAbsolutePath().normalize();
      }
    } catch (IOException e) {
      log.error("[RESOLVER] Error resolving manifest path for: {}", manifestName, e);
    }

    log.debug("[RESOLVER] Manifest not found: {}", manifestName);
    return null;
  }

  /**
   * Gets all manifest files from both external and internal paths. External manifests take priority if same name exists
   * in both locations.
   */
  public Stream<Path> getAllManifestPaths() throws IOException {
    if (resourceProperties.citizens() == null) {
      return Stream.empty();
    }

    Path path = resourceProperties.citizens().getFile().toPath().toAbsolutePath().normalize();
    if (Files.exists(path) && Files.isDirectory(path)) {
      log.info("[RESOLVER] Loading manifests from path: {}", path);
      return getManifestsFromPath(path).stream();
    }

    log.warn("[RESOLVER] Citizens path does not exist or is not a directory: {}", path);
    return Stream.empty();
  }

  private List<Path> getManifestsFromPath(Path path) throws IOException {
    if (!Files.exists(path)) {
      log.debug("[RESOLVER] Path does not exist: {}", path);
      return List.of();
    }

    try (var stream = Files.list(path)) {
      return stream
          .filter(p -> p.toString().endsWith(".md"))
          .sorted()
          .toList();
    }
  }

  /**
   * Checks if external manifests directory exists and is accessible.
   */
  public boolean hasExternalManifests() {
    return resourceProperties.citizens() != null && resourceProperties.citizens().exists();
  }
}

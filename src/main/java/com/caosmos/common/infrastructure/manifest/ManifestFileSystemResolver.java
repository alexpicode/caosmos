package com.caosmos.common.infrastructure.manifest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * File system resolver for manifest files implementing overlay strategy. Responsibility: Navigate file system and
 * resolve absolute paths with priority rules.
 */
@Component
@Slf4j
public class ManifestFileSystemResolver {

  private final Path externalManifestsPath;
  private final Path internalManifestsPath;

  public ManifestFileSystemResolver(ManifestProperties manifestProperties) {
    this.externalManifestsPath = Paths.get(System.getProperty("user.dir"))
                                      .resolve(manifestProperties.getExternalPath());
    this.internalManifestsPath = Paths.get(System.getProperty("user.dir"))
                                      .resolve(manifestProperties.getInternalPath());
  }

  /**
   * Finds manifest path using overlay system (external first, then internal).
   *
   * @param manifestName Name of the manifest file
   * @return Path to the manifest file, or null if not found
   */
  public Path findManifestPath(String manifestName) {
    Path externalPath = externalManifestsPath.resolve(manifestName);
    if (Files.exists(externalPath)) {
      log.debug("[RESOLVER] Found manifest in external path: {}", externalPath);
      return externalPath;
    }

    Path internalPath = internalManifestsPath.resolve(manifestName);
    if (Files.exists(internalPath)) {
      log.debug("[RESOLVER] Found manifest in internal path: {}", internalPath);
      return internalPath;
    }

    log.debug("[RESOLVER] Manifest not found: {}", manifestName);
    return null;
  }

  /**
   * Gets all manifest files from both external and internal paths. External manifests take priority if same name exists
   * in both locations.
   */
  public Stream<Path> getAllManifestPaths() throws IOException {
    // First collect all external manifests
    Set<String> externalManifestNames = new HashSet<>();
    Stream<Path> externalManifests = getManifestsFromPath(externalManifestsPath)
        .stream()
        .peek(path -> externalManifestNames.add(path.getFileName().toString()));

    // Then collect internal manifests that don't exist in external
    Stream<Path> internalManifests = getManifestsFromPath(internalManifestsPath)
        .stream()
        .filter(internalPath -> !externalManifestNames.contains(internalPath.getFileName().toString()));

    return Stream.concat(externalManifests, internalManifests);
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
    return Files.exists(externalManifestsPath) && Files.isDirectory(externalManifestsPath);
  }
}

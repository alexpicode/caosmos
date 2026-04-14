package com.caosmos.directors.application;

import com.caosmos.directors.domain.model.CacheKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheKeyGenerator {

  public CacheKey generate(
      String verb, SortedSet<String> toolTags,
      SortedSet<String> targetTags, SortedSet<String> envTags
  ) {

    // Construct a deterministic string representation of the action context.
    // It's vital we use SortedSets to guarantee the same elements in different order
    // produce the exact same string (e.g. [A, B] == [B, A] -> output "[A,B]").
    StringBuilder sb = new StringBuilder();
    sb.append(verb != null ? verb.toUpperCase() : "UNKNOWN").append("|");

    sb.append("[");
    if (toolTags != null) {
      sb.append(String.join(",", toolTags));
    }
    sb.append("]|");

    sb.append("[");
    if (targetTags != null) {
      sb.append(String.join(",", targetTags));
    }
    sb.append("]|");

    sb.append("[");
    if (envTags != null) {
      sb.append(String.join(",", envTags));
    }
    sb.append("]");

    String keyString = sb.toString();
    log.debug("[CACHE KEY] Generating key from string: {}", keyString);

    try {
      // Apply SHA-256 for a fixed-length constraint and indexing efficiency
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
      return new CacheKey(bytesToHex(encodedhash));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}

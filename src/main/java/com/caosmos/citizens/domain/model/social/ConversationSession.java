package com.caosmos.citizens.domain.model.social;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ConversationSession {

  private final String sessionId;
  private final String initiatorId;
  private final Map<String, String> participants;        // id → name (everyone who has participated)
  private final Set<String> activeParticipantIds;        // those who are still active

  @Setter
  private ConversationPhase phase;

  private int turnsWithoutResponse;
  private String lastSpeakerId;
  private long lastActivityTick;
  private final LinkedList<DialogueLine> history = new LinkedList<>();

  public ConversationSession(
      String sessionId,
      String initiatorId,
      String initiatorName,
      long startTick
  ) {
    this.sessionId = sessionId;
    this.initiatorId = initiatorId;
    this.participants = new ConcurrentHashMap<>();
    this.activeParticipantIds = ConcurrentHashMap.newKeySet();
    this.participants.put(initiatorId, initiatorName);
    this.activeParticipantIds.add(initiatorId);
    this.phase = ConversationPhase.INITIATED;
    this.turnsWithoutResponse = 0;
    this.lastSpeakerId = initiatorId;
    this.lastActivityTick = startTick;
  }

  /**
   * Adds a participant. Returns false if session is full.
   */
  public synchronized boolean addParticipant(String id, String name, int maxParticipants) {
    if (activeParticipantIds.size() >= maxParticipants) {
      return false;
    }
    participants.put(id, name);
    activeParticipantIds.add(id);
    return true;
  }

  /**
   * Removes a participant. Returns remaining active count.
   */
  public synchronized int removeParticipant(String id) {
    activeParticipantIds.remove(id);
    return activeParticipantIds.size();
  }

  public boolean isParticipant(String id) {
    return activeParticipantIds.contains(id);
  }

  /**
   * Returns names of all active participants except the excluded one.
   */
  public Map<String, String> getOthers(String excludeId) {
    Map<String, String> others = new HashMap<>(participants);
    others.remove(excludeId);
    // Only include active ones
    others.keySet().retainAll(activeParticipantIds);
    return others;
  }

  public List<String> getActiveParticipantNames() {
    return activeParticipantIds.stream()
        .map(id -> participants.getOrDefault(id, "Unknown"))
        .toList();
  }

  public int getActiveParticipantCount() {
    return activeParticipantIds.size();
  }

  public void incrementTurnsWithoutResponse() {
    this.turnsWithoutResponse++;
  }

  public void addDialogue(DialogueLine line) {
    this.history.add(line);
    if (this.history.size() > 6) {
      this.history.removeFirst();
    }

    // If the last speaker is different from the new one, we assume it's a response
    if (this.lastSpeakerId != null && !this.lastSpeakerId.equals(line.speakerId())) {
      this.phase = ConversationPhase.ACTIVE; // Dialogue established
    }

    this.lastSpeakerId = line.speakerId();
    this.lastActivityTick = line.tick();
    this.turnsWithoutResponse = 0; // Reset turns without response
  }
}

package com.caosmos.citizens.domain.model.social;

import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ConversationSession {

  private final String sessionId;
  private final String initiatorId;
  private final String partnerId;
  private final String partnerName;

  @Setter
  private ConversationPhase phase;

  private int turnsWithoutResponse;
  private String lastSpeakerId;
  private long lastActivityTick;
  private final LinkedList<DialogueLine> history = new LinkedList<>();

  public ConversationSession(
      String sessionId,
      String initiatorId,
      String partnerId,
      String partnerName,
      long startTick
  ) {
    this.sessionId = sessionId;
    this.initiatorId = initiatorId;
    this.partnerId = partnerId;
    this.partnerName = partnerName;
    this.phase = ConversationPhase.INITIATED;
    this.turnsWithoutResponse = 0;
    this.lastSpeakerId = initiatorId;
    this.lastActivityTick = startTick;
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

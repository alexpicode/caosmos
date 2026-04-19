package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.CollisionResult;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.GatewayTransition;
import com.caosmos.common.domain.model.world.SpeechElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.ZoneMetadata;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

public interface WorldPort {

  void addObjectTag(String objectId, String tag);

  void removeObjectTag(String objectId, String tag);

  void updateObjectDescription(String objectId, String description);

  void transformObject(String objectId, String newEntityType, Set<String> newTags);

  ItemData removeObject(String objectId);

  void spawnObject(Vector3 pos, ItemData data);

  boolean isWalkable(Vector3 pos);

  void interactWithObject(String objectId);

  Optional<Vector3> getObjectPosition(String objectId);

  Optional<WorldElement> getObject(String objectId);

  Optional<GatewayTransition> getGatewayTransition(String gatewayId, String currentZoneId);

  String getZoneName(String zoneId);

  boolean isNearObjectWithTag(Vector3 position, String tag, double maxDistance);

  boolean checkCollision(Vector3 position);

  List<String> getZoneTagsAt(Vector3 position);

  boolean isNearObject(Vector3 position, String objectId, double maxDistance);

  void spawnSpeech(SpeechElement speech);

  void consumeSpeech(String speechId);

  SortedSet<EnvironmentImpactTag> getNormalizedEnvironmentTags(String zoneId);

  Set<String> getObjectTags(String objectId);

  CollisionResult validateMovement(Vector3 from, Vector3 to, String zoneId);

  boolean canSeeEntireZone(Vector3 observerPos, String zoneId, double visionRadius);

  Optional<ZoneMetadata> getZoneMetadata(String zoneId);
}
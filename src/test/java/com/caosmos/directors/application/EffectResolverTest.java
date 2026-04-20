package com.caosmos.directors.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.EconomyPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.MutationType;
import com.caosmos.common.domain.model.actions.StateMutation;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.directors.domain.model.ItemTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EffectResolverTest {

  private WorldPort worldPort;
  private CitizenPort citizenPort;
  private EconomyPort economyPort;
  private SpawnRegistryConfig registryConfig;
  private EffectResolver effectResolver;
  private UUID citizenId;

  @BeforeEach
  void setUp() {
    worldPort = mock(WorldPort.class);
    citizenPort = mock(CitizenPort.class);
    economyPort = mock(EconomyPort.class);
    registryConfig = mock(SpawnRegistryConfig.class);
    effectResolver = new EffectResolver(worldPort, citizenPort, economyPort, registryConfig, new ObjectMapper());
    citizenId = UUID.randomUUID();
  }

  @Test
  void shouldResolveAddTag() {
    StateMutation mut = new StateMutation("obj1", MutationType.ADD_TAG, "burning", null);
    effectResolver.resolve(citizenId, List.of(mut));
    verify(worldPort).addObjectTag("obj1", "burning");
  }

  @Test
  void shouldResolveRemoveTag() {
    StateMutation mut = new StateMutation("obj1", MutationType.REMOVE_TAG, "wet", null);
    effectResolver.resolve(citizenId, List.of(mut));
    verify(worldPort).removeObjectTag("obj1", "wet");
  }

  @Test
  void shouldResolveDestroyWithFallback() {
    ItemData removedItem = new ItemData(
        "obj1",
        "Log",
        java.util.Set.of("burnable"),
        "RESOURCE",
        "A heavy wooden log",
        0.1,
        null,
        null,
        null
    );
    when(worldPort.removeObject("obj1")).thenReturn(removedItem);
    when(registryConfig.getDestructionFallbacks()).thenReturn(Map.of("burnable", "ASH"));
    ItemTemplate ashTemplate = new ItemTemplate();
    ashTemplate.setName("Ash");
    ashTemplate.setTags(List.of("ash"));
    ashTemplate.setCategory("RESOURCE");
    ashTemplate.setRadius(0.1);
    when(registryConfig.getSpawnables()).thenReturn(Map.of("ASH", ashTemplate));

    WorldElement mockElement = mock(WorldElement.class);
    when(mockElement.getPosition()).thenReturn(new Vector3(10, 0, 10));
    when(mockElement.getZoneId()).thenReturn("test_zone");
    when(worldPort.getObject("obj1")).thenReturn(java.util.Optional.of(mockElement));

    StateMutation mut = new StateMutation("obj1", MutationType.DESTROY, null, null);
    effectResolver.resolve(citizenId, List.of(mut));

    verify(worldPort).removeObject("obj1");
    verify(worldPort).spawnObject(eq(new Vector3(10, 0, 10)), eq("test_zone"), any(ItemData.class));
  }

  @Test
  void shouldResolveModifyCitizenVitality() {
    StateMutation mut = new StateMutation(null, MutationType.MODIFY_CITIZEN, "vitality", "-15");
    effectResolver.resolve(citizenId, List.of(mut));
    verify(citizenPort).applyDamage(citizenId, 15.0);
  }

  @Test
  void shouldClampModifyCitizenDelta() {
    StateMutation mut = new StateMutation(null, MutationType.MODIFY_CITIZEN, "vitality", "-50");
    effectResolver.resolve(citizenId, List.of(mut));
    verify(citizenPort).applyDamage(citizenId, 20.0); // Clamped to 20
  }

  @Test
  void shouldResolveSpawnTier1() {
    ItemTemplate template = new ItemTemplate();
    template.setName("Firewood");
    template.setTags(List.of("wood"));
    template.setCategory("RESOURCE");
    template.setRadius(0.15);
    when(registryConfig.getSpawnables()).thenReturn(Map.of("FIREWOOD", template));
    when(citizenPort.getPosition(citizenId)).thenReturn(new Vector3(5, 0, 5));
    when(citizenPort.getCurrentZoneId(citizenId)).thenReturn("residential_zone");

    StateMutation mut = new StateMutation(null, MutationType.SPAWN, "FIREWOOD", null);
    effectResolver.resolve(citizenId, List.of(mut));

    verify(worldPort).spawnObject(eq(new Vector3(5, 0, 5)), eq("residential_zone"), any(ItemData.class));
  }

  @Test
  void shouldResolveModifyCitizenCoins() {
    StateMutation mut = new StateMutation(null, MutationType.MODIFY_CITIZEN, "coins", "100");
    effectResolver.resolve(citizenId, List.of(mut));
    verify(economyPort).addCoins(citizenId, 20.0); // Clamped to 20 by EffectResolver
  }

  @Test
  void shouldResolveModifyCitizenCoinsNegative() {
    StateMutation mut = new StateMutation(null, MutationType.MODIFY_CITIZEN, "coins", "-10");
    effectResolver.resolve(citizenId, List.of(mut));
    verify(economyPort).subtractCoins(citizenId, 10.0);
  }
}

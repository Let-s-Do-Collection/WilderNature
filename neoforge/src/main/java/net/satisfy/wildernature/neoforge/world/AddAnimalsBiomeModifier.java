package net.satisfy.wildernature.neoforge.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.satisfy.wildernature.neoforge.registry.WilderNatureBiomeModifiers;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.TagsRegistry;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class AddAnimalsBiomeModifier implements BiomeModifier {
    private static final Set<EntityType<?>> registeredEntities = new HashSet<>();

    private static <T extends Mob> void registerEntity(EntityType<T> type, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<T> predicate) {
        if (!registeredEntities.contains(type)) {
            SpawnPlacements.register(type, SpawnPlacementTypes.ON_GROUND, heightmapType, predicate);
            registeredEntities.add(type);
        }
    }

    public static void registerEntities() {
        registerEntity(EntityRegistry.SQUIRREL.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.OWL.get(), Heightmap.Types.MOTION_BLOCKING, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.TURKEY.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.RACCOON.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.PELICAN.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.DEER.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.RED_WOLF.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.BOAR.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.BISON.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.DOG.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.MINISHEEP.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.PENGUIN.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.CASSOWARY.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.HEDGEHOG.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityRegistry.FLAMINGO.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase.equals(Phase.ADD)) {
            registerEntities();
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DEER, EntityRegistry.DEER.get(), 12, 2, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_RACCOON, EntityRegistry.RACCOON.get(), 8, 2, 3);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SQUIRREL, EntityRegistry.SQUIRREL.get(), 8, 2, 2);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_RED_WOLF, EntityRegistry.RED_WOLF.get(), 10, 3, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_OWL, EntityRegistry.OWL.get(), 12, 3, 3);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BOAR, EntityRegistry.BOAR.get(), 14, 5, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BISON, EntityRegistry.BISON.get(), 10, 3, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_TURKEY, EntityRegistry.TURKEY.get(), 12, 3, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_PELICAN, EntityRegistry.PELICAN.get(), 5, 3, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DOG, EntityRegistry.DOG.get(), 4, 1, 1);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_MINISHEEP, EntityRegistry.MINISHEEP.get(), 8, 2, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_PENGUIN, EntityRegistry.PENGUIN.get(), 10, 2, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_CASSOWARY, EntityRegistry.CASSOWARY.get(), 12, 3, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_HEDGEHOG, EntityRegistry.HEDGEHOG.get(), 9, 2, 3);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_FLAMINGO, EntityRegistry.FLAMINGO.get(), 10, 4, 6);
            addMobSpawn(builder, biome, BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);

        }
    }


    void addMobSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, Holder<Biome> biome, TagKey<Biome> tag, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        if (biome.is(tag)) {
            builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(entityType, weight, minGroupSize, maxGroupSize));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return WilderNatureBiomeModifiers.ADD_ANIMALS_CODEC.get();
    }
}

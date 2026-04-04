package net.satisfy.wildernature.neoforge.core.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import net.satisfy.wildernature.neoforge.core.registry.WilderNatureBiomeModifiers;
import org.jetbrains.annotations.NotNull;

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
        registerEntity(EntityTypeRegistry.SQUIRREL.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.OWL.get(), Heightmap.Types.MOTION_BLOCKING, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.TURKEY.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.RACCOON.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.DEER.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.SWIFT_FOX.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.BOAR.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.BISON.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.DOG.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.MINISHEEP.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.CASSOWARY.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
        registerEntity(EntityTypeRegistry.HEDGEHOG.get(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AmbientCreature::checkMobSpawnRules);
    }

    @Override
    public void modify(@NotNull Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase.equals(Phase.ADD)) {
            registerEntities();
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DEER, EntityTypeRegistry.DEER.get(), 12, 2, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_RACCOON, EntityTypeRegistry.RACCOON.get(), 8, 2, 3);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SQUIRREL, EntityTypeRegistry.SQUIRREL.get(), 8, 2, 2);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SWIFT_FOX, EntityTypeRegistry.SWIFT_FOX.get(), 10, 3, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_OWL, EntityTypeRegistry.OWL.get(), 12, 3, 3);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BOAR, EntityTypeRegistry.BOAR.get(), 14, 5, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BISON, EntityTypeRegistry.BISON.get(), 10, 3, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_TURKEY, EntityTypeRegistry.TURKEY.get(), 12, 3, 5);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DOG, EntityTypeRegistry.DOG.get(), 4, 1, 1);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_MINISHEEP, EntityTypeRegistry.MINISHEEP.get(), 8, 2, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_CASSOWARY, EntityTypeRegistry.CASSOWARY.get(), 12, 3, 4);
            addMobSpawn(builder, biome, TagsRegistry.SPAWNS_HEDGEHOG, EntityTypeRegistry.HEDGEHOG.get(), 9, 2, 3);
            addMobSpawn(builder, biome, BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);

        }
    }


    void addMobSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, Holder<Biome> biome, TagKey<Biome> tag, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        if (biome.is(tag)) {
            builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(entityType, weight, minGroupSize, maxGroupSize));
        }
    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return WilderNatureBiomeModifiers.ADD_ANIMALS_CODEC.get();
    }
}

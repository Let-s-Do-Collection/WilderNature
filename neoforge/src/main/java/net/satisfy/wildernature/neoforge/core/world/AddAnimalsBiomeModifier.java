package net.satisfy.wildernature.neoforge.core.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.satisfy.wildernature.core.entity.animal.tameable.ScorpionEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import net.satisfy.wildernature.neoforge.core.registry.WilderNatureBiomeModifiers;
import net.satisfy.wildernature.neoforge.core.registry.WilderNatureConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class AddAnimalsBiomeModifier implements BiomeModifier {
    private static final Set<EntityType<?>> registeredEntities = new HashSet<>();

    private static <T extends Mob> void registerEntity(EntityType<T> entityType, SpawnPlacements.SpawnPredicate<T> predicate) {
        if (!registeredEntities.contains(entityType)) {
            SpawnPlacements.register(entityType, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate);
            registeredEntities.add(entityType);
        }
    }

    public static void registerEntities() {
        registerEntity(EntityTypeRegistry.SCORPION.get(), ScorpionEntity::checkScorpionSpawnRules);
        registerEntity(EntityTypeRegistry.LION.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.BEAVER.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.GIRAFFE.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.SQUIRREL.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.TURKEY.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.RACCOON.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.DEER.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.SWIFT_FOX.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.BOAR.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.BISON.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.DOG.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.MINISHEEP.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.CASSOWARY.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.HEDGEHOG.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.HIPPO.get(), Animal::checkAnimalSpawnRules);
        registerEntity(EntityTypeRegistry.ELEPHANT.get(), Animal::checkAnimalSpawnRules);
    }

    @Override
    public void modify(@NotNull Holder<Biome> biome, @NotNull Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase != Phase.ADD) return;

        registerEntities();

        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.LION.get(), WilderNatureConfig.lionSpawnWeight, WilderNatureConfig.lionMinGroupSize, WilderNatureConfig.lionMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.ELEPHANT.get(), WilderNatureConfig.elephantSpawnWeight, WilderNatureConfig.elephantMinGroupSize, WilderNatureConfig.elephantMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.GIRAFFE.get(), WilderNatureConfig.giraffeSpawnWeight, WilderNatureConfig.giraffeMinGroupSize, WilderNatureConfig.giraffeMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DEER, EntityTypeRegistry.DEER.get(), WilderNatureConfig.deerSpawnWeight, WilderNatureConfig.deerMinGroupSize, WilderNatureConfig.deerMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_RACCOON, EntityTypeRegistry.RACCOON.get(), WilderNatureConfig.raccoonSpawnWeight, WilderNatureConfig.raccoonMinGroupSize, WilderNatureConfig.raccoonMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SQUIRREL, EntityTypeRegistry.SQUIRREL.get(), WilderNatureConfig.squirrelSpawnWeight, WilderNatureConfig.squirrelMinGroupSize, WilderNatureConfig.squirrelMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SWIFT_FOX, EntityTypeRegistry.SWIFT_FOX.get(), WilderNatureConfig.swiftFoxSpawnWeight, WilderNatureConfig.swiftFoxMinGroupSize, WilderNatureConfig.swiftFoxMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BOAR, EntityTypeRegistry.BOAR.get(), WilderNatureConfig.boarSpawnWeight, WilderNatureConfig.boarMinGroupSize, WilderNatureConfig.boarMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BISON, EntityTypeRegistry.BISON.get(), WilderNatureConfig.bisonSpawnWeight, WilderNatureConfig.bisonMinGroupSize, WilderNatureConfig.bisonMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_TURKEY, EntityTypeRegistry.TURKEY.get(), WilderNatureConfig.turkeySpawnWeight, WilderNatureConfig.turkeyMinGroupSize, WilderNatureConfig.turkeyMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DOG, EntityTypeRegistry.DOG.get(), WilderNatureConfig.dogSpawnWeight, WilderNatureConfig.dogMinGroupSize, WilderNatureConfig.dogMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_MINISHEEP, EntityTypeRegistry.MINISHEEP.get(), WilderNatureConfig.minisheepSpawnWeight, WilderNatureConfig.minisheepMinGroupSize, WilderNatureConfig.minisheepMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_CASSOWARY, EntityTypeRegistry.CASSOWARY.get(), WilderNatureConfig.cassowarySpawnWeight, WilderNatureConfig.cassowaryMinGroupSize, WilderNatureConfig.cassowaryMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SCORPION, EntityTypeRegistry.SCORPION.get(), WilderNatureConfig.scorpionSpawnWeight, WilderNatureConfig.scorpionMinGroupSize, WilderNatureConfig.scorpionMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_HEDGEHOG, EntityTypeRegistry.HEDGEHOG.get(), WilderNatureConfig.hedgehogSpawnWeight, WilderNatureConfig.hedgehogMinGroupSize, WilderNatureConfig.hedgehogMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_RIVER, EntityTypeRegistry.HIPPO.get(), WilderNatureConfig.hippoSpawnWeight, WilderNatureConfig.hippoMinGroupSize, WilderNatureConfig.hippoMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);
        addMobSpawn(builder, biome, BiomeTags.IS_RIVER, EntityTypeRegistry.BEAVER.get(), WilderNatureConfig.beaverSpawnWeight, WilderNatureConfig.beaverMinGroupSize, WilderNatureConfig.beaverMaxGroupSize);
    }

    void addMobSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, Holder<Biome> biome, TagKey<Biome> tag, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        if (!biome.is(tag) || weight <= 0 || minGroupSize <= 0 || maxGroupSize <= 0 || minGroupSize > maxGroupSize) return;
        builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(entityType, weight, minGroupSize, maxGroupSize));
    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return WilderNatureBiomeModifiers.ADD_ANIMALS_CODEC.get();
    }
}
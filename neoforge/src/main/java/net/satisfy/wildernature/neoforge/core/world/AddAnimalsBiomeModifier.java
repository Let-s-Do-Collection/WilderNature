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
import net.satisfy.wildernature.neoforge.core.config.WilderNatureNeoForgeConfig;
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

        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.LION.get(), WilderNatureNeoForgeConfig.lionSpawnWeight, WilderNatureNeoForgeConfig.lionMinGroupSize, WilderNatureNeoForgeConfig.lionMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.ELEPHANT.get(), WilderNatureNeoForgeConfig.elephantSpawnWeight, WilderNatureNeoForgeConfig.elephantMinGroupSize, WilderNatureNeoForgeConfig.elephantMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_SAVANNA, EntityTypeRegistry.GIRAFFE.get(), WilderNatureNeoForgeConfig.giraffeSpawnWeight, WilderNatureNeoForgeConfig.giraffeMinGroupSize, WilderNatureNeoForgeConfig.giraffeMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DEER, EntityTypeRegistry.DEER.get(), WilderNatureNeoForgeConfig.deerSpawnWeight, WilderNatureNeoForgeConfig.deerMinGroupSize, WilderNatureNeoForgeConfig.deerMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_RACCOON, EntityTypeRegistry.RACCOON.get(), WilderNatureNeoForgeConfig.raccoonSpawnWeight, WilderNatureNeoForgeConfig.raccoonMinGroupSize, WilderNatureNeoForgeConfig.raccoonMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SQUIRREL, EntityTypeRegistry.SQUIRREL.get(), WilderNatureNeoForgeConfig.squirrelSpawnWeight, WilderNatureNeoForgeConfig.squirrelMinGroupSize, WilderNatureNeoForgeConfig.squirrelMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SWIFT_FOX, EntityTypeRegistry.SWIFT_FOX.get(), WilderNatureNeoForgeConfig.swiftFoxSpawnWeight, WilderNatureNeoForgeConfig.swiftFoxMinGroupSize, WilderNatureNeoForgeConfig.swiftFoxMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BOAR, EntityTypeRegistry.BOAR.get(), WilderNatureNeoForgeConfig.boarSpawnWeight, WilderNatureNeoForgeConfig.boarMinGroupSize, WilderNatureNeoForgeConfig.boarMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_BISON, EntityTypeRegistry.BISON.get(), WilderNatureNeoForgeConfig.bisonSpawnWeight, WilderNatureNeoForgeConfig.bisonMinGroupSize, WilderNatureNeoForgeConfig.bisonMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_TURKEY, EntityTypeRegistry.TURKEY.get(), WilderNatureNeoForgeConfig.turkeySpawnWeight, WilderNatureNeoForgeConfig.turkeyMinGroupSize, WilderNatureNeoForgeConfig.turkeyMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_DOG, EntityTypeRegistry.DOG.get(), WilderNatureNeoForgeConfig.dogSpawnWeight, WilderNatureNeoForgeConfig.dogMinGroupSize, WilderNatureNeoForgeConfig.dogMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_MINISHEEP, EntityTypeRegistry.MINISHEEP.get(), WilderNatureNeoForgeConfig.miniSheepSpawnWeight, WilderNatureNeoForgeConfig.miniSheepMinGroupSize, WilderNatureNeoForgeConfig.miniSheepMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_CASSOWARY, EntityTypeRegistry.CASSOWARY.get(), WilderNatureNeoForgeConfig.cassowarySpawnWeight, WilderNatureNeoForgeConfig.cassowaryMinGroupSize, WilderNatureNeoForgeConfig.cassowaryMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_SCORPION, EntityTypeRegistry.SCORPION.get(), WilderNatureNeoForgeConfig.scorpionSpawnWeight, WilderNatureNeoForgeConfig.scorpionMinGroupSize, WilderNatureNeoForgeConfig.scorpionMaxGroupSize);
        addMobSpawn(builder, biome, TagsRegistry.SPAWNS_HEDGEHOG, EntityTypeRegistry.HEDGEHOG.get(), WilderNatureNeoForgeConfig.hedgehogSpawnWeight, WilderNatureNeoForgeConfig.hedgehogMinGroupSize, WilderNatureNeoForgeConfig.hedgehogMaxGroupSize);
        addMobSpawn(builder, biome, BiomeTags.IS_RIVER, EntityTypeRegistry.HIPPO.get(), WilderNatureNeoForgeConfig.hippoSpawnWeight, WilderNatureNeoForgeConfig.hippoMinGroupSize, WilderNatureNeoForgeConfig.hippoMaxGroupSize);
        if (WilderNatureNeoForgeConfig.ENABLE_JUNGLE_FROGS.get()) {
            addMobSpawn(builder, biome, BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);
        }
        addMobSpawn(builder, biome, BiomeTags.IS_RIVER, EntityTypeRegistry.BEAVER.get(), WilderNatureNeoForgeConfig.beaverSpawnWeight, WilderNatureNeoForgeConfig.beaverMinGroupSize, WilderNatureNeoForgeConfig.beaverMaxGroupSize);
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
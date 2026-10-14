package net.satisfy.wildernature.fabric;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.function.Predicate;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.animal.tameable.ScorpionEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import net.satisfy.wildernature.fabric.compat.TrinketsCompat;
import net.satisfy.wildernature.fabric.core.config.WilderNatureFabricConfig;
import net.satisfy.wildernature.fabric.core.world.PlacedFeatures;

public class WilderNatureFabric implements ModInitializer {
    private static Predicate<BiomeSelectionContext> getWilderNatureSelector() {
        return BiomeSelectors.tag(TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_patch_hazelnut_bush")));
    }

    private static Predicate<BiomeSelectionContext> getHollowCacheSelector() {
        return BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(WilderNatureFabricConfig.class, GsonConfigSerializer::new);
        WilderNature.init();
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsCompat.init();
        }
        registerFuel();
        addSpawns();
        addBiomeModification();
        UseItemCallback.EVENT.register((player, level, interactionHand) -> {
            if (!WilderNatureUtil.Truffling.isTruffled(player.getItemInHand(interactionHand))) {
                return InteractionResultHolder.pass(ItemStack.EMPTY);
            }
            WilderNatureUtil.Truffling.FoodValue additionalFoodValues = WilderNatureUtil.Truffling.getAdditionalFoodValue();
            FoodProperties foodProperties = player.getItemInHand(interactionHand).get(DataComponents.FOOD);
            if (foodProperties != null) {
                foodProperties.nutrition = foodProperties.nutrition() + (int) (foodProperties.nutrition() * 0.20F) + additionalFoodValues.nutrition();
                foodProperties.saturation = foodProperties.saturation() + (foodProperties.saturation() * .20F) + additionalFoodValues.saturationModifier();
            }
            return InteractionResultHolder.success(player.getItemInHand(interactionHand));
        });
    }

    void addBiomeModification() {
        WilderNatureFabricConfig.WorldSettings settings = AutoConfig.getConfigHolder(WilderNatureFabricConfig.class).getConfig().world;
        BiomeModification world = BiomeModifications.create(WilderNature.identifier("world_features"));
        Predicate<BiomeSelectionContext> spawnsPatchHazelnutBush = getWilderNatureSelector();
        Predicate<BiomeSelectionContext> spawnTermiteMound = BiomeSelectors.tag(BiomeTags.IS_SAVANNA);
        Predicate<BiomeSelectionContext> spawnHollowCache = getHollowCacheSelector();

        if (settings.enableHazelnutBushes) {
            world.add(ModificationPhase.ADDITIONS, spawnsPatchHazelnutBush, context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.PATCH_HAZELNUT_BUSH));
        } else {
            world.add(ModificationPhase.REMOVALS, spawnsPatchHazelnutBush, context -> context.getGenerationSettings().removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.PATCH_HAZELNUT_BUSH));
        }

        if (settings.enableTermiteMounds) {
            world.add(ModificationPhase.ADDITIONS, spawnTermiteMound, context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.TERMITE_MOUND));
        } else {
            world.add(ModificationPhase.REMOVALS, spawnTermiteMound, context -> context.getGenerationSettings().removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.TERMITE_MOUND));
        }

        if (settings.enableHollowCaches) {
            world.add(ModificationPhase.ADDITIONS, spawnHollowCache, context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.HOLLOW_CACHE));
        } else {
            world.add(ModificationPhase.REMOVALS, spawnHollowCache, context -> context.getGenerationSettings().removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.HOLLOW_CACHE));
        }
    }

    private void registerFuel() {
        FuelRegistry.INSTANCE.add(ObjectRegistry.FISH_OIL.get(), 1600);
        FuelRegistry.INSTANCE.add(ObjectRegistry.WOODMEAL.get(), 150);
    }

    void addSpawns() {
        WilderNatureFabricConfig config = AutoConfig.getConfigHolder(WilderNatureFabricConfig.class).getConfig();
        WilderNatureFabricConfig.SpawningSettings spawning = config.spawning;

        addMobSpawn(TagsRegistry.SPAWNS_DEER, EntityTypeRegistry.DEER.get(), spawning.deerSpawnWeight, spawning.deerMinGroupSize, spawning.deerMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_RACCOON, EntityTypeRegistry.RACCOON.get(), spawning.raccoonSpawnWeight, spawning.raccoonMinGroupSize, spawning.raccoonMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_SQUIRREL, EntityTypeRegistry.SQUIRREL.get(), spawning.squirrelSpawnWeight, spawning.squirrelMinGroupSize, spawning.squirrelMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_SWIFT_FOX, EntityTypeRegistry.SWIFT_FOX.get(), spawning.swiftFoxSpawnWeight, spawning.swiftFoxMinGroupSize, spawning.swiftFoxMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_BOAR, EntityTypeRegistry.BOAR.get(), spawning.boarSpawnWeight, spawning.boarMinGroupSize, spawning.boarMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_BISON, EntityTypeRegistry.BISON.get(), spawning.bisonSpawnWeight, spawning.bisonMinGroupSize, spawning.bisonMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_TURKEY, EntityTypeRegistry.TURKEY.get(), spawning.turkeySpawnWeight, spawning.turkeyMinGroupSize, spawning.turkeyMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_DOG, EntityTypeRegistry.DOG.get(), spawning.dogSpawnWeight, spawning.dogMinGroupSize, spawning.dogMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_MINISHEEP, EntityTypeRegistry.MINISHEEP.get(), spawning.miniSheepSpawnWeight, spawning.miniSheepMinGroupSize, spawning.miniSheepMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_CASSOWARY, EntityTypeRegistry.CASSOWARY.get(), spawning.cassowarySpawnWeight, spawning.cassowaryMinGroupSize, spawning.cassowaryMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_HEDGEHOG, EntityTypeRegistry.HEDGEHOG.get(), spawning.hedgehogSpawnWeight, spawning.hedgehogMinGroupSize, spawning.hedgehogMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_SCORPION, EntityTypeRegistry.SCORPION.get(), spawning.scorpionSpawnWeight, spawning.scorpionMinGroupSize, spawning.scorpionMaxGroupSize);
        addMobSpawn(BiomeTags.IS_SAVANNA, EntityTypeRegistry.ELEPHANT.get(), spawning.elephantSpawnWeight, spawning.elephantMinGroupSize, spawning.elephantMaxGroupSize);
        addMobSpawn(BiomeTags.IS_SAVANNA, EntityTypeRegistry.GIRAFFE.get(), spawning.giraffeSpawnWeight, spawning.giraffeMinGroupSize, spawning.giraffeMaxGroupSize);
        addMobSpawn(BiomeTags.IS_RIVER, EntityTypeRegistry.HIPPO.get(), spawning.hippoSpawnWeight, spawning.hippoMinGroupSize, spawning.hippoMaxGroupSize);
        addMobSpawn(BiomeTags.IS_RIVER, EntityTypeRegistry.BEAVER.get(), spawning.beaverSpawnWeight, spawning.beaverMinGroupSize, spawning.beaverMaxGroupSize);
        addMobSpawn(BiomeTags.IS_SAVANNA, EntityTypeRegistry.LION.get(), spawning.lionSpawnWeight, spawning.lionMinGroupSize, spawning.lionMaxGroupSize);

        if (config.world.removeSavannaAnimals) {
            removeSpawn(BiomeTags.IS_SAVANNA, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.world.removeForestAnimals) {
            removeSpawn(BiomeTags.IS_FOREST, List.of(EntityType.PIG, EntityType.CHICKEN));
        }
        if (config.world.removeSwampAnimals) {
            removeSpawn(ConventionalBiomeTags.SWAMP, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.world.removeJungleAnimals) {
            removeSpawn(BiomeTags.IS_JUNGLE, List.of(EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.world.enableJungleFrogs) {
            addMobSpawn(BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);
        }

        SpawnPlacements.register(EntityTypeRegistry.SCORPION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScorpionEntity::checkScorpionSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.LION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.BEAVER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.ELEPHANT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.GIRAFFE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.TURKEY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.RACCOON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.SWIFT_FOX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.BOAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.BISON.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.DOG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.MINISHEEP.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.CASSOWARY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.HEDGEHOG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.HIPPO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
    }

    void addMobSpawn(TagKey<Biome> tag, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        if (weight <= 0 || minGroupSize <= 0 || maxGroupSize <= 0 || minGroupSize > maxGroupSize) return;
        BiomeModifications.addSpawn(biomeSelector -> biomeSelector.hasTag(tag), MobCategory.CREATURE, entityType, weight, minGroupSize, maxGroupSize);
    }

    void removeSpawn(TagKey<Biome> tag, List<EntityType<?>> entityTypes) {
        entityTypes.forEach(entityType -> {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            Preconditions.checkState(BuiltInRegistries.ENTITY_TYPE.containsKey(id), "Unregistered entity tier: %s", entityType);
            BiomeModifications.create(id).add(ModificationPhase.REMOVALS, biomeSelector -> biomeSelector.hasTag(tag), context -> context.getSpawnSettings().removeSpawnsOfEntityType(entityType));
        });
    }
}
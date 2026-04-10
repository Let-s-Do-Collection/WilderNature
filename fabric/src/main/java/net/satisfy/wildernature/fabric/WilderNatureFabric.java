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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import net.satisfy.wildernature.fabric.core.config.ConfigFabric;
import net.satisfy.wildernature.fabric.core.world.PlacedFeatures;

public class WilderNatureFabric implements ModInitializer {
    private static Predicate<BiomeSelectionContext> getWilderNatureSelector() {
        return BiomeSelectors.tag(TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_patch_hazelnut_bush")));
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigFabric.class, GsonConfigSerializer::new);
        WilderNature.init();
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
        ConfigFabric config = AutoConfig.getConfigHolder(ConfigFabric.class).getConfig();
        BiomeModification world = BiomeModifications.create(WilderNature.identifier("world_features"));
        Predicate<BiomeSelectionContext> spawnsPatchHazelnutBush = getWilderNatureSelector();

        if (config.spawnHazelnutBush) {
            world.add(ModificationPhase.ADDITIONS, spawnsPatchHazelnutBush, context -> context.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.PATCH_HAZELNUT_BUSH));
        } else {
            world.add(ModificationPhase.REMOVALS, spawnsPatchHazelnutBush, context -> context.getGenerationSettings().removeFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatures.PATCH_HAZELNUT_BUSH));
        }
    }

    private void registerFuel() {
        FuelRegistry.INSTANCE.add(ObjectRegistry.FISH_OIL.get(), 1600);
    }

    void addSpawns() {
        ConfigFabric config = AutoConfig.getConfigHolder(ConfigFabric.class).getConfig();

        addMobSpawn(TagsRegistry.SPAWNS_DEER, EntityTypeRegistry.DEER.get(), config.DeerSpawnWeight, config.DeerMinGroupSize, config.DeerMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_RACCOON, EntityTypeRegistry.RACCOON.get(), config.RaccoonSpawnWeight, config.RaccoonMinGroupSize, config.RaccoonMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_SQUIRREL, EntityTypeRegistry.SQUIRREL.get(), config.SquirrelSpawnWeight, config.SquirrelMinGroupSize, config.SquirrelMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_SWIFT_FOX, EntityTypeRegistry.SWIFT_FOX.get(), config.SwiftfoxSpawnWeight, config.SwiftfoxMinGroupSize, config.SwiftfoxMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_OWL, EntityTypeRegistry.OWL.get(), config.OwlSpawnWeight, config.OwlMinGroupSize, config.OwlMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_BOAR, EntityTypeRegistry.BOAR.get(), config.BoarSpawnWeight, config.BoarMinGroupSize, config.BoarMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_BISON, EntityTypeRegistry.BISON.get(), config.BisonSpawnWeight, config.BisonMinGroupSize, config.BisonMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_TURKEY, EntityTypeRegistry.TURKEY.get(), config.TurkeySpawnWeight, config.TurkeyMinGroupSize, config.TurkeyMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_DOG, EntityTypeRegistry.DOG.get(), config.DogSpawnWeight, config.DogMinGroupSize, config.DogMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_MINISHEEP, EntityTypeRegistry.MINISHEEP.get(), config.MiniSheepSpawnWeight, config.MiniSheepMinGroupSize, config.MiniSheepMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_CASSOWARY, EntityTypeRegistry.CASSOWARY.get(), config.CassowarySpawnWeight, config.CassowaryMinGroupSize, config.CassowaryMaxGroupSize);
        addMobSpawn(TagsRegistry.SPAWNS_HEDGEHOG, EntityTypeRegistry.HEDGEHOG.get(), config.HedgehogSpawnWeight, config.HedgehogMinGroupSize, config.HedgehogMaxGroupSize);
        addMobSpawn(BiomeTags.IS_SAVANNA, EntityTypeRegistry.ELEPHANT.get(), config.ElephantSpawnWeight, config.ElephantMinGroupSize, config.ElephantMaxGroupSize);
        addMobSpawn(BiomeTags.IS_SAVANNA, EntityTypeRegistry.GIRAFFE.get(), config.GiraffeSpawnWeight, config.GiraffeMinGroupSize, config.GiraffeMaxGroupSize);
        addMobSpawn(BiomeTags.IS_RIVER, EntityTypeRegistry.HIPPO.get(), config.HippoSpawnWeight, config.HippoMinGroupSize, config.HippoMaxGroupSize);
        addMobSpawn(BiomeTags.IS_RIVER, EntityTypeRegistry.BEAVER.get(), config.BeaverSpawnWeight, config.BeaverMinGroupSize, config.BeaverMaxGroupSize);

        if (config.removeSavannaAnimals) {
            removeSpawn(BiomeTags.IS_SAVANNA, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.removeForestAnimals) {
            removeSpawn(BiomeTags.IS_FOREST, List.of(EntityType.PIG, EntityType.CHICKEN));
        }
        if (config.removeSwampAnimals) {
            removeSpawn(ConventionalBiomeTags.SWAMP, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.removeJungleAnimals) {
            removeSpawn(BiomeTags.IS_JUNGLE, List.of(EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
        }
        if (config.addJungleAnimals) {
            addMobSpawn(BiomeTags.IS_JUNGLE, EntityType.FROG, 8, 3, 4);
        }
        SpawnPlacements.register(EntityTypeRegistry.BEAVER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.ELEPHANT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.GIRAFFE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.SQUIRREL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
        SpawnPlacements.register(EntityTypeRegistry.OWL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);
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
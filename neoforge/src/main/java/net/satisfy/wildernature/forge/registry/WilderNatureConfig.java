package net.satisfy.wildernature.forge.registry;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.io.File;

public class WilderNatureConfig {
    public static ForgeConfigSpec COMMON_CONFIG;

    public static final ForgeConfigSpec.BooleanValue REMOVE_SAVANNA_ANIMALS;
    public static final ForgeConfigSpec.BooleanValue REMOVE_SWAMP_ANIMALS;
    public static final ForgeConfigSpec.BooleanValue REMOVE_JUNGLE_ANIMALS;
    public static final ForgeConfigSpec.BooleanValue REMOVE_FOREST_ANIMALS;
    public static final ForgeConfigSpec.BooleanValue ADD_JUNGLE_ANIMALS;
    public static final ForgeConfigSpec.BooleanValue SPAWN_HAZELNUT_BUSH;
    public static final ForgeConfigSpec.IntValue PELICAN_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue PELICAN_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue PELICAN_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue DEER_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue DEER_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue DEER_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue RACCOON_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue RACCOON_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue RACCOON_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue SQUIRREL_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue SQUIRREL_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue SQUIRREL_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue RED_WOLF_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue RED_WOLF_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue RED_WOLF_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue OWL_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue OWL_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue OWL_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue BOAR_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue BOAR_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue BOAR_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue BISON_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue BISON_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue BISON_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue TURKEY_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue TURKEY_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue TURKEY_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue DOG_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue DOG_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue DOG_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue MINISHEEP_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue MINISHEEP_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue MINISHEEP_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue PENGUIN_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue PENGUIN_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue PENGUIN_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue CASSOWARY_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue CASSOWARY_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue CASSOWARY_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue HEDGEHOG_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue HEDGEHOG_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue HEDGEHOG_MAX_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue FLAMINGO_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue FLAMINGO_MIN_GROUP_SIZE;
    public static final ForgeConfigSpec.IntValue FLAMINGO_MAX_GROUP_SIZE;
    
    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

        REMOVE_SAVANNA_ANIMALS = COMMON_BUILDER.comment("Remove Savanna Animals").define("removeSavannaAnimals", true);
        REMOVE_SWAMP_ANIMALS = COMMON_BUILDER.comment("Remove Swamp Animals").define("removeSwampAnimals", true);
        REMOVE_JUNGLE_ANIMALS = COMMON_BUILDER.comment("Remove Jungle Animals").define("removeJungleAnimals", true);
        REMOVE_FOREST_ANIMALS = COMMON_BUILDER.comment("Remove Forest Animals").define("removeForestAnimals", true);
        ADD_JUNGLE_ANIMALS = COMMON_BUILDER.comment("Add Jungle Animals").define("addJungleAnimals", true);
        SPAWN_HAZELNUT_BUSH = COMMON_BUILDER.comment("Spawn Hazelnut Bush").define("spawnHazelnutBush", true);
        PELICAN_SPAWN_WEIGHT = COMMON_BUILDER.comment("Pelican Spawn Weight").defineInRange("pelicanSpawnWeight", 5, 0, 1000);
        PELICAN_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Pelican Min Group Size").defineInRange("pelicanMinGroupSize", 3, 1, 10);
        PELICAN_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Pelican Max Group Size").defineInRange("pelicanMaxGroupSize", 5, 1, 10);
        DEER_SPAWN_WEIGHT = COMMON_BUILDER.comment("Deer Spawn Weight").defineInRange("deerSpawnWeight", 12, 0, 1000);
        DEER_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Deer Min Group Size").defineInRange("deerMinGroupSize", 2, 1, 10);
        DEER_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Deer Max Group Size").defineInRange("deerMaxGroupSize", 4, 1, 10);
        RACCOON_SPAWN_WEIGHT = COMMON_BUILDER.comment("Raccoon Spawn Weight").defineInRange("raccoonSpawnWeight", 8, 0, 1000);
        RACCOON_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Raccoon Min Group Size").defineInRange("raccoonMinGroupSize", 2, 1, 10);
        RACCOON_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Raccoon Max Group Size").defineInRange("raccoonMaxGroupSize", 3, 1, 10);
        SQUIRREL_SPAWN_WEIGHT = COMMON_BUILDER.comment("Squirrel Spawn Weight").defineInRange("squirrelSpawnWeight", 8, 0, 1000);
        SQUIRREL_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Squirrel Min Group Size").defineInRange("squirrelMinGroupSize", 2, 1, 10);
        SQUIRREL_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Squirrel Max Group Size").defineInRange("squirrelMaxGroupSize", 2, 1, 10);
        RED_WOLF_SPAWN_WEIGHT = COMMON_BUILDER.comment("Red Wolf Spawn Weight").defineInRange("redWolfSpawnWeight", 10, 0, 1000);
        RED_WOLF_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Red Wolf Min Group Size").defineInRange("redWolfMinGroupSize", 2, 1, 10);
        RED_WOLF_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Red Wolf Max Group Size").defineInRange("redWolfMaxGroupSize", 4, 1, 10);
        OWL_SPAWN_WEIGHT = COMMON_BUILDER.comment("Owl Spawn Weight").defineInRange("owlSpawnWeight", 12, 0, 1000);
        OWL_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Owl Min Group Size").defineInRange("owlMinGroupSize", 3, 1, 10);
        OWL_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Owl Max Group Size").defineInRange("owlMaxGroupSize", 3, 1, 10);
        BOAR_SPAWN_WEIGHT = COMMON_BUILDER.comment("Boar Spawn Weight").defineInRange("boarSpawnWeight", 14, 0, 1000);
        BOAR_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Boar Min Group Size").defineInRange("boarMinGroupSize", 4, 1, 10);
        BOAR_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Boar Max Group Size").defineInRange("boarMaxGroupSize", 5, 1, 10);
        BISON_SPAWN_WEIGHT = COMMON_BUILDER.comment("Bison Spawn Weight").defineInRange("bisonSpawnWeight", 10, 0, 1000);
        BISON_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Bison Min Group Size").defineInRange("bisonMinGroupSize", 3, 1, 10);
        BISON_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Bison Max Group Size").defineInRange("bisonMaxGroupSize", 5, 1, 10);
        TURKEY_SPAWN_WEIGHT = COMMON_BUILDER.comment("Turkey Spawn Weight").defineInRange("turkeySpawnWeight", 12, 0, 1000);
        TURKEY_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Turkey Min Group Size").defineInRange("turkeyMinGroupSize", 3, 1, 10);
        TURKEY_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Turkey Max Group Size").defineInRange("turkeyMaxGroupSize", 5, 1, 10);
        DOG_SPAWN_WEIGHT = COMMON_BUILDER.comment("Dog Spawn Weight").defineInRange("dogSpawnWeight", 2, 0, 1000);
        DOG_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Dog Min Group Size").defineInRange("dogMinGroupSize", 1, 1, 10);
        DOG_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Dog Max Group Size").defineInRange("dogMaxGroupSize", 1, 1, 10);
        MINISHEEP_SPAWN_WEIGHT = COMMON_BUILDER.comment("MiniSheep Spawn Weight").defineInRange("minisheepSpawnWeight", 8, 0, 1000);
        MINISHEEP_MIN_GROUP_SIZE = COMMON_BUILDER.comment("MiniSheep Min Group Size").defineInRange("minisheepMinGroupSize", 2, 1, 10);
        MINISHEEP_MAX_GROUP_SIZE = COMMON_BUILDER.comment("MiniSheep Max Group Size").defineInRange("minisheepMaxGroupSize", 4, 1, 10);
        PENGUIN_SPAWN_WEIGHT = COMMON_BUILDER.comment("Penguin Spawn Weight").defineInRange("penguinSpawnWeight", 10, 0, 1000);
        PENGUIN_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Penguin Min Group Size").defineInRange("penguinMinGroupSize", 2, 1, 10);
        PENGUIN_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Penguin Max Group Size").defineInRange("penguinMaxGroupSize", 5, 1, 10);
        CASSOWARY_SPAWN_WEIGHT = COMMON_BUILDER.comment("Cassowary Spawn Weight").defineInRange("cassowarySpawnWeight", 12, 0, 1000);
        CASSOWARY_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Cassowary Min Group Size").defineInRange("cassowaryMinGroupSize", 3, 1, 10);
        CASSOWARY_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Cassowary Max Group Size").defineInRange("cassowaryMaxGroupSize", 4, 1, 10);
        FLAMINGO_SPAWN_WEIGHT = COMMON_BUILDER.comment("Flamingo Spawn Weight").defineInRange("flamingoSpawnWeight", 9, 0, 1000);
        FLAMINGO_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Flamingo Min Group Size").defineInRange("flamingoMinGroupSize", 3, 1, 10);
        FLAMINGO_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Flamingo Max Group Size").defineInRange("flamingoMaxGroupSize", 6, 1, 10);
        HEDGEHOG_SPAWN_WEIGHT = COMMON_BUILDER.comment("Hedgehog Spawn Weight").defineInRange("hedgehogSpawnWeight", 10, 0, 1000);
        HEDGEHOG_MIN_GROUP_SIZE = COMMON_BUILDER.comment("Hedgehog Min Group Size").defineInRange("hedgehogMinGroupSize", 2, 1, 10);
        HEDGEHOG_MAX_GROUP_SIZE = COMMON_BUILDER.comment("Hedgehog Max Group Size").defineInRange("hedgehogMaxGroupSize", 4, 1, 10);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) { }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) { }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().preserveInsertionOrder().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
}

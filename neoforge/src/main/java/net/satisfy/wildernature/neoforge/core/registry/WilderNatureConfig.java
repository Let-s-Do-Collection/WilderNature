package net.satisfy.wildernature.neoforge.core.registry;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class WilderNatureConfig {
    public static ModConfigSpec COMMON_CONFIG;

    public static final ModConfigSpec.BooleanValue REMOVE_SAVANNA_ANIMALS;
    public static final ModConfigSpec.BooleanValue REMOVE_SWAMP_ANIMALS;
    public static final ModConfigSpec.BooleanValue REMOVE_JUNGLE_ANIMALS;
    public static final ModConfigSpec.BooleanValue REMOVE_FOREST_ANIMALS;
    public static final ModConfigSpec.BooleanValue ADD_JUNGLE_ANIMALS;
    public static final ModConfigSpec.BooleanValue SPAWN_HAZELNUT_BUSH;
    public static final ModConfigSpec.BooleanValue SPAWN_TERMITE_MOUND;

    public static final ModConfigSpec.IntValue LION_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue LION_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue LION_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue BEAVER_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue BEAVER_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue BEAVER_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue ELEPHANT_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue ELEPHANT_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue ELEPHANT_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue HIPPO_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue HIPPO_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue HIPPO_MAX_GROUP_SIZE;
    
    public static final ModConfigSpec.IntValue GIRAFFE_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue GIRAFFE_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue GIRAFFE_MAX_GROUP_SIZE;
    
    public static final ModConfigSpec.IntValue DEER_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue DEER_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue DEER_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue RACCOON_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue RACCOON_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue RACCOON_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue SQUIRREL_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue SQUIRREL_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue SQUIRREL_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue SWIFT_FOX_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue SWIFT_FOX_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue SWIFT_FOX_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue BOAR_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue BOAR_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue BOAR_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue BISON_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue BISON_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue BISON_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue TURKEY_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue TURKEY_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue TURKEY_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue DOG_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue DOG_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue DOG_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue MINISHEEP_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue MINISHEEP_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue MINISHEEP_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue CASSOWARY_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue CASSOWARY_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue CASSOWARY_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue HEDGEHOG_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue HEDGEHOG_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue HEDGEHOG_MAX_GROUP_SIZE;

    public static boolean removeSavannaAnimals;
    public static boolean removeSwampAnimals;
    public static boolean removeJungleAnimals;
    public static boolean removeForestAnimals;
    public static boolean addJungleAnimals;
    public static boolean spawnHazelnutBush;
    public static boolean spawnTermiteMound;

    public static int lionSpawnWeight;
    public static int lionMinGroupSize;
    public static int lionMaxGroupSize;
    
    public static int beaverSpawnWeight;
    public static int beaverMinGroupSize;
    public static int beaverMaxGroupSize;
    
    public static int elephantSpawnWeight;
    public static int elephantMinGroupSize;
    public static int elephantMaxGroupSize;
    
    public static int hippoSpawnWeight;
    public static int hippoMinGroupSize;
    public static int hippoMaxGroupSize;
    
    public static int giraffeSpawnWeight;
    public static int giraffeMinGroupSize;
    public static int giraffeMaxGroupSize;
    
    public static int deerSpawnWeight;
    public static int deerMinGroupSize;
    public static int deerMaxGroupSize;

    public static int raccoonSpawnWeight;
    public static int raccoonMinGroupSize;
    public static int raccoonMaxGroupSize;

    public static int squirrelSpawnWeight;
    public static int squirrelMinGroupSize;
    public static int squirrelMaxGroupSize;

    public static int redWolfSpawnWeight;
    public static int redWolfMinGroupSize;
    public static int redWolfMaxGroupSize;

    public static int boarSpawnWeight;
    public static int boarMinGroupSize;
    public static int boarMaxGroupSize;

    public static int bisonSpawnWeight;
    public static int bisonMinGroupSize;
    public static int bisonMaxGroupSize;

    public static int turkeySpawnWeight;
    public static int turkeyMinGroupSize;
    public static int turkeyMaxGroupSize;

    public static int dogSpawnWeight;
    public static int dogMinGroupSize;
    public static int dogMaxGroupSize;

    public static int minisheepSpawnWeight;
    public static int minisheepMinGroupSize;
    public static int minisheepMaxGroupSize;

    public static int cassowarySpawnWeight;
    public static int cassowaryMinGroupSize;
    public static int cassowaryMaxGroupSize;

    public static int hedgehogSpawnWeight;
    public static int hedgehogMinGroupSize;
    public static int hedgehogMaxGroupSize;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        REMOVE_SAVANNA_ANIMALS = builder.define("removeSavannaAnimals", true);
        REMOVE_SWAMP_ANIMALS = builder.define("removeSwampAnimals", true);
        REMOVE_JUNGLE_ANIMALS = builder.define("removeJungleAnimals", true);
        REMOVE_FOREST_ANIMALS = builder.define("removeForestAnimals", true);
        ADD_JUNGLE_ANIMALS = builder.define("addJungleAnimals", true);
        SPAWN_HAZELNUT_BUSH = builder.define("spawnHazelnutBush", true);
        SPAWN_TERMITE_MOUND = builder.define("spawnTermiteMound", true);

        LION_SPAWN_WEIGHT = builder.defineInRange("lionSpawnWeight", 12, 0, 1000);
        LION_MIN_GROUP_SIZE = builder.defineInRange("lionMinGroupSize", 3, 1, 10);
        LION_MAX_GROUP_SIZE = builder.defineInRange("lionMaxGroupSize", 5, 1, 10);
        
        BEAVER_SPAWN_WEIGHT = builder.defineInRange("beaverSpawnWeight", 11, 0, 1000);
        BEAVER_MIN_GROUP_SIZE = builder.defineInRange("beaverMinGroupSize", 2, 1, 10);
        BEAVER_MAX_GROUP_SIZE = builder.defineInRange("beaverMaxGroupSize", 3, 1, 10);
        
        ELEPHANT_SPAWN_WEIGHT = builder.defineInRange("elephantSpawnWeight", 12, 0, 1000);
        ELEPHANT_MIN_GROUP_SIZE = builder.defineInRange("elephantMinGroupSize", 3, 1, 10);
        ELEPHANT_MAX_GROUP_SIZE = builder.defineInRange("elephantMaxGroupSize", 5, 1, 10);
        
        HIPPO_SPAWN_WEIGHT = builder.defineInRange("hippoSpawnWeight", 10, 0, 1000);
        HIPPO_MIN_GROUP_SIZE = builder.defineInRange("hippoMinGroupSize", 3, 1, 10);
        HIPPO_MAX_GROUP_SIZE = builder.defineInRange("hippoMaxGroupSize", 5, 1, 10);
        
        GIRAFFE_SPAWN_WEIGHT = builder.defineInRange("giraffeSpawnWeight", 12, 0, 1000);
        GIRAFFE_MIN_GROUP_SIZE = builder.defineInRange("giraffeMinGroupSize", 2, 1, 10);
        GIRAFFE_MAX_GROUP_SIZE = builder.defineInRange("giraffeMaxGroupSize", 4, 1, 10);
        
        DEER_SPAWN_WEIGHT = builder.defineInRange("deerSpawnWeight", 12, 0, 1000);
        DEER_MIN_GROUP_SIZE = builder.defineInRange("deerMinGroupSize", 2, 1, 10);
        DEER_MAX_GROUP_SIZE = builder.defineInRange("deerMaxGroupSize", 4, 1, 10);

        RACCOON_SPAWN_WEIGHT = builder.defineInRange("raccoonSpawnWeight", 8, 0, 1000);
        RACCOON_MIN_GROUP_SIZE = builder.defineInRange("raccoonMinGroupSize", 2, 1, 10);
        RACCOON_MAX_GROUP_SIZE = builder.defineInRange("raccoonMaxGroupSize", 3, 1, 10);

        SQUIRREL_SPAWN_WEIGHT = builder.defineInRange("squirrelSpawnWeight", 8, 0, 1000);
        SQUIRREL_MIN_GROUP_SIZE = builder.defineInRange("squirrelMinGroupSize", 2, 1, 10);
        SQUIRREL_MAX_GROUP_SIZE = builder.defineInRange("squirrelMaxGroupSize", 2, 1, 10);

        SWIFT_FOX_SPAWN_WEIGHT = builder.defineInRange("redWolfSpawnWeight", 10, 0, 1000);
        SWIFT_FOX_MIN_GROUP_SIZE = builder.defineInRange("redWolfMinGroupSize", 2, 1, 10);
        SWIFT_FOX_MAX_GROUP_SIZE = builder.defineInRange("redWolfMaxGroupSize", 4, 1, 10);
        
        BOAR_SPAWN_WEIGHT = builder.defineInRange("boarSpawnWeight", 14, 0, 1000);
        BOAR_MIN_GROUP_SIZE = builder.defineInRange("boarMinGroupSize", 4, 1, 10);
        BOAR_MAX_GROUP_SIZE = builder.defineInRange("boarMaxGroupSize", 5, 1, 10);

        BISON_SPAWN_WEIGHT = builder.defineInRange("bisonSpawnWeight", 10, 0, 1000);
        BISON_MIN_GROUP_SIZE = builder.defineInRange("bisonMinGroupSize", 3, 1, 10);
        BISON_MAX_GROUP_SIZE = builder.defineInRange("bisonMaxGroupSize", 5, 1, 10);

        TURKEY_SPAWN_WEIGHT = builder.defineInRange("turkeySpawnWeight", 12, 0, 1000);
        TURKEY_MIN_GROUP_SIZE = builder.defineInRange("turkeyMinGroupSize", 3, 1, 10);
        TURKEY_MAX_GROUP_SIZE = builder.defineInRange("turkeyMaxGroupSize", 5, 1, 10);

        DOG_SPAWN_WEIGHT = builder.defineInRange("dogSpawnWeight", 2, 0, 1000);
        DOG_MIN_GROUP_SIZE = builder.defineInRange("dogMinGroupSize", 1, 1, 10);
        DOG_MAX_GROUP_SIZE = builder.defineInRange("dogMaxGroupSize", 1, 1, 10);

        MINISHEEP_SPAWN_WEIGHT = builder.defineInRange("minisheepSpawnWeight", 8, 0, 1000);
        MINISHEEP_MIN_GROUP_SIZE = builder.defineInRange("minisheepMinGroupSize", 2, 1, 10);
        MINISHEEP_MAX_GROUP_SIZE = builder.defineInRange("minisheepMaxGroupSize", 4, 1, 10);

        CASSOWARY_SPAWN_WEIGHT = builder.defineInRange("cassowarySpawnWeight", 12, 0, 1000);
        CASSOWARY_MIN_GROUP_SIZE = builder.defineInRange("cassowaryMinGroupSize", 3, 1, 10);
        CASSOWARY_MAX_GROUP_SIZE = builder.defineInRange("cassowaryMaxGroupSize", 4, 1, 10);

        HEDGEHOG_SPAWN_WEIGHT = builder.defineInRange("hedgehogSpawnWeight", 10, 0, 1000);
        HEDGEHOG_MIN_GROUP_SIZE = builder.defineInRange("hedgehogMinGroupSize", 2, 1, 10);
        HEDGEHOG_MAX_GROUP_SIZE = builder.defineInRange("hedgehogMaxGroupSize", 4, 1, 10);

        COMMON_CONFIG = builder.build();
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != COMMON_CONFIG) return;
        sync();
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != COMMON_CONFIG) return;
        sync();
    }

    public static void sync() {
        removeSavannaAnimals = REMOVE_SAVANNA_ANIMALS.get();
        removeSwampAnimals = REMOVE_SWAMP_ANIMALS.get();
        removeJungleAnimals = REMOVE_JUNGLE_ANIMALS.get();
        removeForestAnimals = REMOVE_FOREST_ANIMALS.get();
        addJungleAnimals = ADD_JUNGLE_ANIMALS.get();
        spawnHazelnutBush = SPAWN_HAZELNUT_BUSH.get();
        spawnTermiteMound = SPAWN_TERMITE_MOUND.get();

        lionSpawnWeight = LION_SPAWN_WEIGHT.get();
        lionMinGroupSize = LION_MIN_GROUP_SIZE.get();
        lionMaxGroupSize = LION_MAX_GROUP_SIZE.get();

        elephantSpawnWeight = ELEPHANT_SPAWN_WEIGHT.get();
        elephantMinGroupSize = ELEPHANT_MIN_GROUP_SIZE.get();
        elephantMaxGroupSize = ELEPHANT_MAX_GROUP_SIZE.get();

        hippoSpawnWeight = HIPPO_SPAWN_WEIGHT.get();
        hippoMinGroupSize = HIPPO_MIN_GROUP_SIZE.get();
        hippoMaxGroupSize = HIPPO_MAX_GROUP_SIZE.get();

        giraffeSpawnWeight = GIRAFFE_SPAWN_WEIGHT.get();
        giraffeMinGroupSize = GIRAFFE_MIN_GROUP_SIZE.get();
        giraffeMaxGroupSize = GIRAFFE_MAX_GROUP_SIZE.get();
        
        deerSpawnWeight = DEER_SPAWN_WEIGHT.get();
        deerMinGroupSize = DEER_MIN_GROUP_SIZE.get();
        deerMaxGroupSize = DEER_MAX_GROUP_SIZE.get();

        raccoonSpawnWeight = RACCOON_SPAWN_WEIGHT.get();
        raccoonMinGroupSize = RACCOON_MIN_GROUP_SIZE.get();
        raccoonMaxGroupSize = RACCOON_MAX_GROUP_SIZE.get();

        squirrelSpawnWeight = SQUIRREL_SPAWN_WEIGHT.get();
        squirrelMinGroupSize = SQUIRREL_MIN_GROUP_SIZE.get();
        squirrelMaxGroupSize = SQUIRREL_MAX_GROUP_SIZE.get();

        redWolfSpawnWeight = SWIFT_FOX_SPAWN_WEIGHT.get();
        redWolfMinGroupSize = SWIFT_FOX_MIN_GROUP_SIZE.get();
        redWolfMaxGroupSize = SWIFT_FOX_MAX_GROUP_SIZE.get();

        beaverSpawnWeight = BEAVER_SPAWN_WEIGHT.get();
        beaverMinGroupSize = BEAVER_MIN_GROUP_SIZE.get();
        beaverMaxGroupSize = BEAVER_MAX_GROUP_SIZE.get();

        boarSpawnWeight = BOAR_SPAWN_WEIGHT.get();
        boarMinGroupSize = BOAR_MIN_GROUP_SIZE.get();
        boarMaxGroupSize = BOAR_MAX_GROUP_SIZE.get();

        bisonSpawnWeight = BISON_SPAWN_WEIGHT.get();
        bisonMinGroupSize = BISON_MIN_GROUP_SIZE.get();
        bisonMaxGroupSize = BISON_MAX_GROUP_SIZE.get();

        turkeySpawnWeight = TURKEY_SPAWN_WEIGHT.get();
        turkeyMinGroupSize = TURKEY_MIN_GROUP_SIZE.get();
        turkeyMaxGroupSize = TURKEY_MAX_GROUP_SIZE.get();

        dogSpawnWeight = DOG_SPAWN_WEIGHT.get();
        dogMinGroupSize = DOG_MIN_GROUP_SIZE.get();
        dogMaxGroupSize = DOG_MAX_GROUP_SIZE.get();

        minisheepSpawnWeight = MINISHEEP_SPAWN_WEIGHT.get();
        minisheepMinGroupSize = MINISHEEP_MIN_GROUP_SIZE.get();
        minisheepMaxGroupSize = MINISHEEP_MAX_GROUP_SIZE.get();

        cassowarySpawnWeight = CASSOWARY_SPAWN_WEIGHT.get();
        cassowaryMinGroupSize = CASSOWARY_MIN_GROUP_SIZE.get();
        cassowaryMaxGroupSize = CASSOWARY_MAX_GROUP_SIZE.get();

        hedgehogSpawnWeight = HEDGEHOG_SPAWN_WEIGHT.get();
        hedgehogMinGroupSize = HEDGEHOG_MIN_GROUP_SIZE.get();
        hedgehogMaxGroupSize = HEDGEHOG_MAX_GROUP_SIZE.get();
    }
}
package net.satisfy.wildernature.fabric.core.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.satisfy.wildernature.WilderNature;

@Config(name = WilderNature.MOD_ID)
@Config.Gui.Background("wildernature:textures/block/rotten_log.png")
public class WilderNatureFabricConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public WorldSettings world = new WorldSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public SpawningSettings spawning = new SpawningSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public AnimalsSettings animals = new AnimalsSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public BountiesSettings bounties = new BountiesSettings();

    @ConfigEntry.Gui.CollapsibleObject
    public MiscSettings misc = new MiscSettings();

    public static class WorldSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean removeSavannaAnimals = true;

        @ConfigEntry.Gui.Tooltip
        public boolean removeSwampAnimals = true;

        @ConfigEntry.Gui.Tooltip
        public boolean removeJungleAnimals = true;

        @ConfigEntry.Gui.Tooltip
        public boolean removeForestAnimals = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableJungleFrogs = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableHazelnutBushes = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableTermiteMounds = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableHollowCaches = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
        public int termiteMoundRarity = 36;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
        public int hollowCacheRarity = 18;
    }

    public static class SpawningSettings {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int scorpionSpawnWeight = 8;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int scorpionMinGroupSize = 1;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int scorpionMaxGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int giraffeSpawnWeight = 13;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int giraffeMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int giraffeMaxGroupSize = 4;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int deerSpawnWeight = 13;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int deerMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int deerMaxGroupSize = 4;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int raccoonSpawnWeight = 12;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int raccoonMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int raccoonMaxGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int squirrelSpawnWeight = 10;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int squirrelMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int squirrelMaxGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int swiftFoxSpawnWeight = 12;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int swiftFoxMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int swiftFoxMaxGroupSize = 4;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int boarSpawnWeight = 14;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int boarMinGroupSize = 4;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int boarMaxGroupSize = 5;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int bisonSpawnWeight = 14;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int bisonMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int bisonMaxGroupSize = 6;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int turkeySpawnWeight = 12;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int turkeyMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int turkeyMaxGroupSize = 5;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int dogSpawnWeight = 5;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int dogMinGroupSize = 1;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int dogMaxGroupSize = 1;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int miniSheepSpawnWeight = 13;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int miniSheepMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int miniSheepMaxGroupSize = 6;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int cassowarySpawnWeight = 7;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int cassowaryMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int cassowaryMaxGroupSize = 5;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int hedgehogSpawnWeight = 13;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int hedgehogMinGroupSize = 1;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int hedgehogMaxGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int hippoSpawnWeight = 14;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int hippoMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int hippoMaxGroupSize = 6;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int elephantSpawnWeight = 10;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int elephantMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int elephantMaxGroupSize = 5;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int beaverSpawnWeight = 11;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int beaverMinGroupSize = 2;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int beaverMaxGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int lionSpawnWeight = 12;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int lionMinGroupSize = 3;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int lionMaxGroupSize = 5;
    }

    public static class AnimalsSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enableTermiteLogEating = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableBeaverDams = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableRaccoonStealing = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableRaccoonDoors = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableBoarRooting = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableDogDigging = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableSquirrelGifts = true;

        @ConfigEntry.Gui.Tooltip
        public boolean enableBisonCharge = true;
    }

    public static class BountiesSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enableBountyBoard = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int bountyMinDaily = 12;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int bountyMaxDaily = 16;

        @ConfigEntry.Gui.Tooltip
        public double bountyExperienceMultiplier = 1.0;
    }

    public static class MiscSettings {
        @ConfigEntry.Gui.Tooltip
        public boolean enableEmoteParticles = true;

        @ConfigEntry.Gui.Tooltip
        public boolean animations = true;
    }

    @Override
    public void validatePostLoad() {
        bounties.bountyExperienceMultiplier = Math.max(0.0, Math.min(10.0, bounties.bountyExperienceMultiplier));
        bounties.bountyMinDaily = Math.max(1, Math.min(32, bounties.bountyMinDaily));
        bounties.bountyMaxDaily = Math.max(bounties.bountyMinDaily, Math.min(32, bounties.bountyMaxDaily));
    }
}

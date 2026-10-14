package net.satisfy.wildernature.platform.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import net.satisfy.wildernature.fabric.core.config.WilderNatureFabricConfig;

public class PlatformHelperImpl {
    private static WilderNatureFabricConfig config() {
        return AutoConfig.getConfigHolder(WilderNatureFabricConfig.class).getConfig();
    }

    public static boolean isTermiteLogEatingEnabled() {
        return config().animals.enableTermiteLogEating;
    }

    public static boolean isBeaverDamsEnabled() {
        return config().animals.enableBeaverDams;
    }

    public static boolean isRaccoonStealingEnabled() {
        return config().animals.enableRaccoonStealing;
    }

    public static boolean isRaccoonDoorsEnabled() {
        return config().animals.enableRaccoonDoors;
    }

    public static boolean isBoarRootingEnabled() {
        return config().animals.enableBoarRooting;
    }

    public static boolean isDogDiggingEnabled() {
        return config().animals.enableDogDigging;
    }

    public static boolean isSquirrelGiftsEnabled() {
        return config().animals.enableSquirrelGifts;
    }

    public static boolean isBisonChargeEnabled() {
        return config().animals.enableBisonCharge;
    }

    public static boolean isBountyBoardEnabled() {
        return config().bounties.enableBountyBoard;
    }

    public static int getBountyMinDaily() {
        return config().bounties.bountyMinDaily;
    }

    public static int getBountyMaxDaily() {
        return config().bounties.bountyMaxDaily;
    }

    public static boolean isEmoteParticlesEnabled() {
        return config().misc.enableEmoteParticles;
    }

    public static boolean isAnimationsEnabled() {
        return config().misc.animations;
    }

    public static int getTermiteMoundRarity() {
        return config().world.termiteMoundRarity;
    }

    public static int getHollowCacheRarity() {
        return config().world.hollowCacheRarity;
    }

    public static double getBountyExperienceMultiplier() {
        return config().bounties.bountyExperienceMultiplier;
    }
}
